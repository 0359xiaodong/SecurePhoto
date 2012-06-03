import webapp2
from google.appengine.ext import db

class SPR_Provider(webapp2.RequestHandler):
    def get(self):
        self.response.headers['Content-Type'] = 'text/plain'
        self.response.out.write('I will get you a SPRoll')
        
class SPR_Record(db.Model):
    time = db.DateTimeProperty(auto_now_add=True)
    user = db.UserProperty()
    device_id = db.StringProperty(default="0")
    spr_id = db.StringProperty()
    validity_date = db.DateTimeProperty()
    
    
    
    
        
        