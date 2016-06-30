#!/usr/bin/env python

import tornado.ioloop
import tornado.web

class ContainerCreateHandler(tornado.web.RequestHandler):
    def post(self):
        print("CREATE POST")
        print(self.request.body)

        self.set_status(201)
        self.set_header("Content-Type", "application/json")
        with open('container_created.json', 'r') as resp:
            self.write(resp.read())

class ContainerKillHandler(tornado.web.RequestHandler):
    def post(self, id):
        print("KILL POST")
        print(self.request.body)

        self.set_status(204)
        self.set_header("Content-Type", "application/json")

def make_app():
    return tornado.web.Application([
        (r"/v1.21/containers/create", ContainerCreateHandler),
        (r"/v1.21/containers/([^/]+)/kill", ContainerKillHandler),
    ])

if __name__ == "__main__":
    app = make_app()
    app.listen(2376)
    tornado.ioloop.IOLoop.current().start()
