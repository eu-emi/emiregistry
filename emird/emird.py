#!/usr/bin/python

# Dependcies:
#   sudo yum install python-simplejson

import signal
import logging

from daemon import Daemon
from time   import sleep
from sys    import exit

from EMIR import EMIRConfiguration, EMIRClient

class emird(Daemon):
  def __init__(self, pid_file, config_file, log_file):

    # Initialize EMIR related components
    try: 
      self.config = EMIRConfiguration(config_file) 
    except Exception, inst: 
      print inst 
      exit(1) 
    self.client = EMIRClient(self.config)

    # Initialize 'emird' logger object
    self.logger = logging.getLogger('emird')
    logger_handler = logging.FileHandler(log_file)
    logger_formatter = logging.Formatter('%(asctime)s %(levelname)s %(message)s')
    logger_handler.setFormatter(logger_formatter)
    self.logger.addHandler(logger_handler)
    self.logger.setLevel(self.config.loglevel)

    # Check wether there is any service entry to be registered
    if not self.config.getServiceEntries():
      self.logger.info("No service entries has been defined")
      exit(0)

    # Check whether the EMIR is URL is valid and the service is available
    try:
      self.logger.debug("EMIR service on url '%s://%s:%s' is running since %s" % (self.config.protocol, self.config.host, self.config.port, self.client.ping()))
    except IOError, err:
      self.logger.error("EMIR service on url '%s://%s:%s' is not available" % (self.config.protocol, self.config.host, self.config.port))
      exit(1)
    except Exception, ex:
      self.logger.error(ex)
      exit(1)

    # Call parent's constructor to daemonize the process
    super(emird,self).__init__(pid_file)

  def stop(self):
    # Delete registered entries
    try:
      self.logger.debug('Deleting reqistered entries')
      self.client.delete()
    except Exception, ex:
      self.logger.error(ex)
      exit(1)
    # Call parents stop() function to stop the daemon itself
    super(emird,self).stop()

  def run(self):
    # Try to send registration message. If do not manage to fall back
    # to update. For example because of already existing registration
    # entries.
    try:
      self.logger.debug('Registering service entries')
      self.client.register()
    except Exception, ex:
      try:
        self.logger.debug('Falling back to initial service entry update')
        self.client.update()
      except Exception, ex:
        self.logger.error("Registration failed: %s" % ex)
        exit(1)
    
    while True:
      # After the successful initial registration wait the selected time
      # (where the configured period is given in minutes so have to be
      # multiplied by 60) then try to send an update message.
      sleep(self.config.period)
      try:
        self.logger.debug('Periodical registration update')
        self.client.update()
      except Exception, ex:
        self.logger.error("Update failed: %s" % ex)
        exit(1)

if __name__ == "__main__":

  # The daemon is started in foreground mode ( = 1) or in the
  # background ( = 0 )
  foreground = 0
  
  # Location of the configuration file. TODO: Should be turn into a
  # configurable option.
  config_file = 'emird.ini'

  # Location of the log file. TODO: Should be turn into a
  # configurable option.
  log_file = '/tmp/emird.log'

  # Location of the pid file. TODO: Should be turn into a
  # configurable option.
  pid_file = '/tmp/emird.pid'

  # Creating and starting the daemon class and process
  myDaemon = emird(pid_file, config_file, log_file)

  if foreground:
    myDaemon.run()
  else:
    myDaemon.start()

  exit(0)
