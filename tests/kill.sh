source test_config.sh

curl -v -X POST http://${CONDUCTOR_HOST}:1337/test/die
echo
