#!/bin/bash
set -e

cd /home/ubuntu/backend
FIRST_DEPLOYMENT=false
S3_BUCKET="techeazy-backend"
TIMESTAMP=$(date -u +"%Y-%m-%d_%H-%M-%S")
CURRENT_DATE=$(date -u +"%Y-%m-%d %H:%M:%S")
CURRENT_USER="Prabal864"
S3_BUILDS_PREFIX="builds"
S3_LOGS_PREFIX="logs"

# Define the new S3 subfolder structure
S3_FAILED_BUILDS="$S3_BUILDS_PREFIX/failed-builds"
S3_RUNNING_BUILDS="$S3_BUILDS_PREFIX/running-builds"
S3_SUCCESS_LOGS="$S3_LOGS_PREFIX/running-dep-logs"
S3_FAILED_LOGS="$S3_LOGS_PREFIX/failed-dep-logs"
S3_ROLLBACK_LOGS="$S3_LOGS_PREFIX/rollback-dep-logs"

# Extract JAR name for tracking
JAR_NAME=$(basename new-parcel.jar)
BUILD_ID="${JAR_NAME%-*}" # Extract build ID/version from JAR name if available

echo "===== Deployment Started at $CURRENT_DATE by $CURRENT_USER ====="
echo "Deploying build: $JAR_NAME"

echo "Checking if this is first-time deployment..."
if [ ! -f parcel.jar ]; then
  echo "First-time deployment detected!"
  FIRST_DEPLOYMENT=true
fi

echo "Stopping any running JAR..."
pkill -f 'java -jar parcel.jar' || echo "No running JAR to stop."

echo "Ensuring port 8080 is free..."
if lsof -i:8080 -t &> /dev/null; then
  echo "Port 8080 is still in use. Forcing termination of the process..."
  lsof -i:8080 -t | xargs kill -9 || echo "Could not kill process on port 8080"
  sleep 3
fi

echo "Checking for AWS CLI..."
if ! command -v aws &> /dev/null; then
  echo "ERROR: AWS CLI not found. Cannot proceed with S3 backup strategy."
  exit 1
fi

if [ -f parcel.jar ] && [ "$FIRST_DEPLOYMENT" = false ]; then
  echo "Backing up current JAR to S3..."
  aws s3 cp parcel.jar s3://$S3_BUCKET/$S3_RUNNING_BUILDS/parcel-$TIMESTAMP.jar || echo "WARNING: Failed to back up current JAR to S3."
else
  echo "No existing JAR to backup (first-time deployment)."
fi

echo "Verifying new JAR integrity..."
if ! jar tf new-parcel.jar > /dev/null 2>&1; then
  echo "ERROR: The new JAR file appears to be corrupt!"
  exit 1
fi

echo "Deploying new JAR..."
cp new-parcel.jar parcel.jar
chmod 755 parcel.jar

echo "Starting new JAR..."
nohup java -jar parcel.jar > logs.txt 2>&1 &
PID=$!
echo "Started with PID: $PID"

if [ "$FIRST_DEPLOYMENT" = true ]; then
  echo "First deployment - waiting longer for initialization (45 seconds)..."
  sleep 45
else
  echo "Waiting for application to start (30 seconds)..."
  sleep 30
fi

# Save the deployment logs before health check
cp logs.txt deployment-logs.txt
echo -e "\n\nDeployment of build: $JAR_NAME (ID: $BUILD_ID) at $CURRENT_DATE by $CURRENT_USER" >> deployment-logs.txt

echo "Health check..."
if curl -s -o /dev/null -w "%{http_code}" http://localhost:8080/actuator/health | grep -q "200"; then
  echo "Deployment successful ✅"
  # Copy JAR to running-builds folder with latest tag
  aws s3 cp parcel.jar s3://$S3_BUCKET/$S3_RUNNING_BUILDS/parcel-latest.jar || echo "WARNING: Failed to upload JAR to S3."
  # Also store with original name for traceability
  aws s3 cp parcel.jar s3://$S3_BUCKET/$S3_RUNNING_BUILDS/$JAR_NAME || echo "WARNING: Failed to upload JAR with original name to S3."
  # Upload logs to successful deployments folder
  aws s3 cp logs.txt s3://$S3_BUCKET/$S3_SUCCESS_LOGS/deployment-$JAR_NAME-$TIMESTAMP.log || echo "S3 log upload failed"
else
  echo "Health check failed ❌"

  # Track the failed build in logs
  echo "FAILED BUILD: $JAR_NAME (ID: $BUILD_ID) at $CURRENT_DATE" >> deployment-logs.txt

  if grep -q "Port 8080 was already in use" logs.txt; then
    echo "ERROR: Application failed due to port conflict:"
    tail -n 20 logs.txt
  fi

  # Check if process is still running before attempting to kill it
  if ps -p $PID > /dev/null 2>&1; then
    echo "Terminating failed process with PID: $PID"
    kill $PID || echo "Failed to kill process"
  else
    echo "Process already terminated (PID: $PID no longer exists)"
  fi

  # Save failed JAR to S3 for debugging
  aws s3 cp parcel.jar s3://$S3_BUCKET/$S3_FAILED_BUILDS/$JAR_NAME-$TIMESTAMP.jar || echo "WARNING: Failed to upload failed JAR to S3."

  if [ "$FIRST_DEPLOYMENT" = true ]; then
    echo "⚠️ First deployment failed. Checking for latest JAR in S3..."

    # Upload deployment logs showing the failure
    aws s3 cp deployment-logs.txt s3://$S3_BUCKET/$S3_FAILED_LOGS/failed-$JAR_NAME-$TIMESTAMP.log || echo "S3 log upload failed"

    # Check if latest JAR exists in S3 before attempting recovery
    if aws s3 ls s3://$S3_BUCKET/$S3_RUNNING_BUILDS/parcel-latest.jar &> /dev/null; then
      echo "Found latest stable JAR in S3. Attempting recovery..."

      # Ensure port is free before starting
      if lsof -i:8080 -t &> /dev/null; then
        echo "Clearing port 8080 before recovery attempt..."
        lsof -i:8080 -t | xargs kill -9 || echo "No process to kill on port 8080"
        sleep 2
      fi

      # Get the latest JAR and run it
      if aws s3 cp s3://$S3_BUCKET/$S3_RUNNING_BUILDS/parcel-latest.jar parcel.jar; then
        # Use a new log file for recovery logs
        nohup java -jar parcel.jar > recovery-logs.txt 2>&1 &
        RECOVERY_PID=$!
        echo "Started recovery JAR with PID: $RECOVERY_PID"

        # Add recovery information to logs
        echo -e "\n\nRECOVERY ATTEMPT after failed deployment of $JAR_NAME at $CURRENT_DATE by $CURRENT_USER" >> recovery-logs.txt

        # Wait and verify the recovered JAR starts correctly
        echo "Waiting for recovered application to start (30 seconds)..."
        sleep 30

        if curl -s -o /dev/null -w "%{http_code}" http://localhost:8080/actuator/health | grep -q "200"; then
          echo "✅ Recovery successful! Running with latest stable JAR from S3."
          # Upload both the deployment logs and recovery logs
          aws s3 cp deployment-logs.txt s3://$S3_BUCKET/$S3_FAILED_LOGS/failed-$JAR_NAME-$TIMESTAMP.log || echo "S3 log upload failed"
          aws s3 cp recovery-logs.txt s3://$S3_BUCKET/$S3_ROLLBACK_LOGS/recovery-after-$JAR_NAME-$TIMESTAMP.log || echo "S3 log upload failed"
        else
          echo "❌ Recovery failed - recovered JAR did not start properly."
          if ps -p $RECOVERY_PID > /dev/null 2>&1; then
            kill $RECOVERY_PID || echo "Failed to kill recovery process"
          else
            echo "Recovery process already terminated (PID: $RECOVERY_PID no longer exists)"
          fi
          aws s3 cp deployment-logs.txt s3://$S3_BUCKET/$S3_FAILED_LOGS/failed-$JAR_NAME-$TIMESTAMP.log || echo "S3 log upload failed"
          aws s3 cp recovery-logs.txt s3://$S3_BUCKET/$S3_FAILED_LOGS/failed-recovery-$JAR_NAME-$TIMESTAMP.log || echo "S3 log upload failed"
          exit 1
        fi
      else
        echo "❌ Failed to download latest JAR from S3."
        aws s3 cp deployment-logs.txt s3://$S3_BUCKET/$S3_FAILED_LOGS/failed-$JAR_NAME-$TIMESTAMP.log || echo "S3 log upload failed"
        exit 1
      fi
    else
      echo "❌ No latest JAR found in S3. Cannot recover automatically."
      aws s3 cp deployment-logs.txt s3://$S3_BUCKET/$S3_FAILED_LOGS/failed-$JAR_NAME-$TIMESTAMP.log || echo "S3 log upload failed"
      exit 1
    fi
  else
    # Upload deployment logs showing the failure
    aws s3 cp deployment-logs.txt s3://$S3_BUCKET/$S3_FAILED_LOGS/failed-$JAR_NAME-$TIMESTAMP.log || echo "S3 log upload failed"

    echo "Rolling back to previous version..."
    if lsof -i:8080 -t &> /dev/null; then
      echo "Forcing termination of process on port 8080 before rollback..."
      lsof -i:8080 -t | xargs kill -9 || echo "No process to kill on port 8080"
      sleep 2
    fi

    if aws s3 ls s3://$S3_BUCKET/$S3_RUNNING_BUILDS/ | grep -q "parcel-"; then
      PREVIOUS_JAR=$(aws s3 ls s3://$S3_BUCKET/$S3_RUNNING_BUILDS/ | grep "parcel-" | sort -r | head -n 1 | awk '{print $4}')
      if [ -n "$PREVIOUS_JAR" ]; then
        echo "Downloading $PREVIOUS_JAR from S3..."
        if aws s3 cp s3://$S3_BUCKET/$S3_RUNNING_BUILDS/$PREVIOUS_JAR parcel.jar; then
          # Use a separate log file for rollback
          nohup java -jar parcel.jar > rollback-logs.txt 2>&1 &
          ROLLBACK_PID=$!
          echo "Started rollback JAR with PID: $ROLLBACK_PID"

          # Add rollback information to logs
          echo -e "\n\nROLLBACK after failed deployment of $JAR_NAME at $CURRENT_DATE by $CURRENT_USER" >> rollback-logs.txt
          echo -e "Rolling back to: $PREVIOUS_JAR" >> rollback-logs.txt

          # Wait for the rollback JAR to start and generate logs
          echo "Waiting for rollback application to start (30 seconds)..."
          sleep 30

          # Verify the health of the rolled back JAR
          if curl -s -o /dev/null -w "%{http_code}" http://localhost:8080/actuator/health | grep -q "200"; then
            echo "✅ Rollback successful! Running with previous version from S3."
          else
            echo "⚠️ Warning: Rolled back JAR is running but health check failed."
          fi

          # Upload both logs to S3
          aws s3 cp deployment-logs.txt s3://$S3_BUCKET/$S3_FAILED_LOGS/failed-$JAR_NAME-$TIMESTAMP.log || echo "S3 log upload failed"
          aws s3 cp rollback-logs.txt s3://$S3_BUCKET/$S3_ROLLBACK_LOGS/rollback-after-$JAR_NAME-$TIMESTAMP.log || echo "S3 log upload failed"
        else
          echo "ERROR: Failed to download previous version from S3!"
          if [ -f parcel-backup.jar ]; then
            cp parcel-backup.jar parcel.jar
            nohup java -jar parcel.jar > local-rollback-logs.txt 2>&1 &
            LOCAL_ROLLBACK_PID=$!

            echo "Started local rollback JAR with PID: $LOCAL_ROLLBACK_PID"
            echo -e "\n\nLOCAL ROLLBACK after failed deployment of $JAR_NAME at $CURRENT_DATE by $CURRENT_USER" >> local-rollback-logs.txt

            echo "Waiting for local rollback to start (30 seconds)..."
            sleep 30

            echo "✅ Rolled back to local backup version with PID: $LOCAL_ROLLBACK_PID"
            aws s3 cp deployment-logs.txt s3://$S3_BUCKET/$S3_FAILED_LOGS/failed-$JAR_NAME-$TIMESTAMP.log || echo "S3 log upload failed"
            aws s3 cp local-rollback-logs.txt s3://$S3_BUCKET/$S3_ROLLBACK_LOGS/local-rollback-after-$JAR_NAME-$TIMESTAMP.log || echo "S3 log upload failed"
          else
            echo "CRITICAL ERROR: No backup available for rollback!"
            exit 1
          fi
        fi
      else
        echo "ERROR: No previous JAR found in S3!"
        exit 1
      fi
    else
      echo "ERROR: No backups found in S3 bucket!"
      exit 1
    fi
  fi
fi

echo "===== Deployment Process Completed at $(date -u +"%Y-%m-%d %H:%M:%S") ====="
echo "Current Date and Time (UTC): $(date -u +"%Y-%m-%d %H:%M:%S")"
echo "Current User's Login: Prabal864"
echo "EC2 will shutdown in 15 minutes"
sudo shutdown -h +15