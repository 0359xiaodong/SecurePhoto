import webapp2
import jinja2
import os

from get_time import Get_Time
from request_spr import SPR_Provider
from hash_submit import Hash_Submit
from hash_retrieve import Hash_Retrieve
from get_spr_validity import Get_SPR_Validity

jinja_environment = jinja2.Environment(
    loader=jinja2.FileSystemLoader(os.path.dirname(__file__)))

SPR_VALIDITY_PATH = r'/get_spr_validity'
SPR_REQUEST_PATH = r'/request_spr'
HASH_SUBMIT_PATH = r'/hash_submit'  
HASH_RETRIEVE_PATH = r'/hash_retrieve'
TIME_RETRIEVE_PATH = r'/get_time'

class MainPage(webapp2.RequestHandler):
    def get(self):
        self.response.headers['Content-Type'] = 'text/html'
        template = jinja_environment.get_template('index.html')
        
        content = { "SPR_REQUEST_PATH" :SPR_REQUEST_PATH,
                    "HASH_SUBMIT_PATH" : HASH_SUBMIT_PATH,
                    "HASH_RETRIEVE_PATH" : HASH_RETRIEVE_PATH,
                    "TIME_RETRIEVE_PATH" : TIME_RETRIEVE_PATH,
                    "SPR_VALIDITY_PATH " : SPR_VALIDITY_PATH,
                   }
        
        self.response.out.write(template.render(content))
        

routes = [(r'/', MainPage),
          (SPR_VALIDITY_PATH, Get_SPR_Validity),
          (SPR_REQUEST_PATH, SPR_Provider),
          (HASH_SUBMIT_PATH, Hash_Submit),
          (HASH_RETRIEVE_PATH, Hash_Retrieve),
          (TIME_RETRIEVE_PATH, Get_Time), ]

app = webapp2.WSGIApplication(routes, debug=True)


