source test_config.sh

curl -X GET http://${CONDUCTOR_HOST}:1337/v1/$1/$2
echo
