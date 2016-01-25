source test_config.sh

curl -X DELETE http://${CONDUCTOR_HOST}:1337/v1/$1/$2
echo
