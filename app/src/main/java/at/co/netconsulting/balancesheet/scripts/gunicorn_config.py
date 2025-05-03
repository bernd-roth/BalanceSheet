import multiprocessing

workers = multiprocessing.cpu_count() * 2 + 1
bind = 'unix:flaskrest.sock'
umask = 0o007
reload = True

#logging
loglevel = 'debug'
accesslog = '/var/log/access_log_app'
acceslogformat ="%(h)s %(l)s %(u)s %(t)s %(r)s %(s)s %(b)s %(f)s %(a)s"
errorlog = '/var/log/error_log_app'