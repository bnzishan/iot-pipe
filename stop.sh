IDS=$(sudo docker ps -a | grep sensemark2benchmark | awk '{print $1}')
sudo docker stop $IDS