source test_config.sh

curl -X POST -H 'Content-Type: application/json' http://${CONDUCTOR_HOST}:1337/v1/$1 -d "$2"
echo
