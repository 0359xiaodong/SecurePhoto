import webapp2

class Hash_Retrieve(webapp2.RequestHandler):
    def get(self):
        self.response.headers['Content-Type'] = 'text/plain'
        self.response.out.write('Here you can retrieve the hash of your photo')