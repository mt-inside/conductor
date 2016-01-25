source test_config.sh

curl -X PUT -H 'Content-Type: application/json' http://${CONDUCTOR_HOST}:1337/v1/$1/$2 -d "$3"
echo
