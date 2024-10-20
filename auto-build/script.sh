 #!/bin/bash

# Note
# Cần phải thay đổi đường dẫn path tới target của local file
# Sửa đường dẫn này thành đường dẫn tới project của mình: C:\Users\haotr\Desktop\FU\2024 Summer\SEP490-G1 (Search sau đó replace là đc nha)
# Nếu không chạy dc thì vào đúng folder đang chứa build script và chạy command: chmod +x push-to-server.sh (Dùng gitbash terminal hoặc ps)

# Variables
DEFAULT_PRJ_PATH="C:\Users\phamh\OneDrive\Desktop\FU\2024\Fall 2024\G27\sep490_g27_pharmacy-management"
OLD_PATH="$DEFAULT_PRJ_PATH""\target\pha-0.0.1-SNAPSHOT.jar"
LOCAL_FILE_PATH="$DEFAULT_PRJ_PATH""\target\app.jar"
REMOTE_USER="root"
REMOTE_HOST="139.180.155.250"
REMOTE_DIR="/home/pha"
SERVICE_NAME="pha-api"

# Check if the target directory exists
if [ ! -d "$DEFAULT_PRJ_PATH" ]; then
    echo "Directory $DEFAULT_PRJ_PATH does not exist."
    exit 1
fi

# Change to the target directory
cd "$DEFAULT_PRJ_PATH" || { echo "Failed to change directory to $DEFAULT_PRJ_PATH"; exit 1; }

# Run Maven clean install
echo "Running 'mvn clean install' in $DEFAULT_PRJ_PATH..."
mvn clean install
sleep 5 &
pid1=$!
wait $pid1

# Rename the file
mv "$OLD_PATH" "$LOCAL_FILE_PATH"
echo "1. Renamed!!!"
echo "--------------------------------------------------------------------------"

# Command to copy file using SCP
scp "$LOCAL_FILE_PATH" "$REMOTE_USER@$REMOTE_HOST:$REMOTE_DIR"
echo "2. End push file!!!"
echo "--------------------------------------------------------------------------"


# Function to perform countdown
countdown() {
#    local seconds=$1
    local seconds=5
    while [ $seconds -gt 0 ]; do
        echo -ne "Time remaining: ${seconds}s\r"
        sleep 1
        ((seconds--))
    done
    echo "Deploy done! Chờ command tự tắt và hưởng thụ!!!!"
}

# Check if SCP was successful
if [ $? -eq 0 ]; then
    echo "File copied successfully. Running command on remote server..."
    # Run command on remote server
    ssh "$REMOTE_USER@$REMOTE_HOST" "systemctl stop ""$SERVICE_NAME""; systemctl start $SERVICE_NAME"
    echo "File successfully copied to $REMOTE_USER@$REMOTE_HOST:$REMOTE_DIR and wait for close...."
    countdown
    sleep 3 &
    pid1=$!
    wait $pid1
else
    echo "Error occurred while copying file"

# Call the countdown function with the provided argument
#countdown "$1"

fi