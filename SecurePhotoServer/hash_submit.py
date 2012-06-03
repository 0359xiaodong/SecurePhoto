import webapp2
from google.appengine.ext import db

class Hash_Submit(webapp2.RequestHandler):
    def get(self):
        self.response.headers['Content-Type'] = 'text/plain'
        self.response.out.write('Here you can submit the hash of your photo')

class SPI_Hash_Entry(db.Model):        
    time = db.DateTimeProperty(auto_now_add=True)
    user = db.UserProperty()
    device_id = db.StringProperty(default="0")
    
    image_id = db.StringProperty()
    image_hash = db.StringProperty()
    
class SPR_Hash_Entry(db.Model):        
    time = db.DateTimeProperty(auto_now_add=True)
    user = db.UserProperty()
    device_id = db.StringProperty(default="0")
    
    validity_date = db.DateTimeProperty()
    spr_id = db.StringProperty()
    image_id = db.StringProperty()
    image_hash = db.StringProperty()      
    
    