#!/usr/bin/python

# Dependcies:
#   sudo yum install python-simplejson

import signal

from daemon import Daemon
from time   import sleep
from sys    import exit

from EMIR import EMIRConfiguration, EMIRClient

class emird(Daemon):
  def __init__(self, pidfile, config_file):

    # Initialize EMIR related components
    try: 
      self.config = EMIRConfiguration(config_file) 
    except Exception, inst: 
      print inst 
      exit(1) 
    self.client = EMIRClient(self.config)

    # Check wether there is any service entry to be registered
    if not self.config.getServiceEntries():
      print "No service entries has been defined"
      exit(0)

    # Check whether the EMIR is URL is valid and the service is available
    try:
      print "EMIR service on url '%s://%s:%s' is running since %s" % (self.config.protocol, self.config.host, self.config.port, self.client.ping())
    except IOError, err:
      print "EMIR service on url '%s://%s:%s' is not available" % (self.config.protocol, self.config.host, self.config.port)
      exit(1)
    except Exception, ex:
      print ex
      exit(1)

    # Call parent's constructor to daemonize the process
    super(emird,self).__init__(pidfile)

  def stop(self):
    # Delete registered entries
    try:
      self.client.delete()
    except Exception, ex:
      print ex
      exit(1)
    # Call parents stop() function to stop the daemon itself
    super(emird,self).stop()

  def run(self):
    # Try to send registration message. If do not manage to fall back
    # to update. For example because of already existing registration
    # entries.
    try:
      self.client.register()
    except Exception, ex:
      try:
        self.client.update()
      except Exception, ex:
        print ex
        exit(1)
    
    while True:
      # After the successful initial registration wait the selected time
      # (where the configured period is given in minutes so have to be
      # multiplied by 60) then try to send an update message.
      sleep(self.config.period * 60)
      try:
        self.client.update()
      except Exception, ex:
        print ex
        exit(1)

if __name__ == "__main__":

  # The daemon is started in foreground mode ( = 1) or in the
  # background ( = 0 )
  foreground = 0
  
  # Location of the configuration file. TODO: Should be turn into a
  # configurable option.
  config_file = 'emird.ini'

  # Creating and starting the daemon class and process
  myDaemon = emird('/tmp/emird.pid', config_file)

  if foreground:
    myDaemon.run()
  else:
    myDaemon.start()

  exit(0)

