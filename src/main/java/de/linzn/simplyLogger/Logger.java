package de.linzn.simplyLogger;

import java.text.SimpleDateFormat;
import java.util.Date;

public class Logger {
    private LogSystem logSystem;


    Logger(LogSystem logSystem) {
        this.logSystem = logSystem;
    }

    public void LIVE(String msg) {
        this.log(Color.WHITE + msg + Color.RESET, LOGLEVEL.DEBUG);
    }

    public void DEBUG(String msg) {
        this.log(Color.PURPLE + msg + Color.RESET, LOGLEVEL.DEBUG);
    }

    public void INFO(String msg) {
        this.log(Color.WHITE + msg + Color.RESET, LOGLEVEL.INFO);
    }

    public void WARNING(String msg) {
        this.log(Color.YELLOW + msg + Color.RESET, LOGLEVEL.WARNING);
    }

    public void ERROR(String msg) {
        this.log(Color.RED + msg + Color.RESET, LOGLEVEL.ERROR);
    }

    private void log(String msg, LOGLEVEL loglevel) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
        String logMSG = dateFormat.format(new Date().getTime()) + "[" + Thread.currentThread().getName() + "] " + msg;
        if (shouldLogged(loglevel)) {
            System.out.println(logMSG);
            if (loglevel != LOGLEVEL.LIVE) {
                this.logSystem.writeToFile(logMSG);
            }
        }
    }


    private boolean shouldLogged(LOGLEVEL loglevel) {
        if (this.logSystem.logLevel == LOGLEVEL.ALL) {
            return true;
         } else if (this.logSystem.logLevel == LOGLEVEL.DEBUG) {
            if (loglevel == LOGLEVEL.LIVE) {
                return true;
            } else if (loglevel == LOGLEVEL.ALL) {
                return false;
            } else if (loglevel == LOGLEVEL.DEBUG) {
                return true;
            } else if (loglevel == LOGLEVEL.INFO) {
                return true;
            } else if (loglevel == LOGLEVEL.WARNING) {
                return true;
            } else if (loglevel == LOGLEVEL.ERROR) {
                return true;
            }
        } else if (this.logSystem.logLevel == LOGLEVEL.INFO) {
            if (loglevel == LOGLEVEL.LIVE) {
                return true;
            } else if (loglevel == LOGLEVEL.ALL) {
                return false;
            } else if (loglevel == LOGLEVEL.DEBUG) {
                return false;
            } else if (loglevel == LOGLEVEL.INFO) {
                return true;
            } else if (loglevel == LOGLEVEL.WARNING) {
                return true;
            } else if (loglevel == LOGLEVEL.ERROR) {
                return true;
            }
        } else if (this.logSystem.logLevel == LOGLEVEL.WARNING) {
            if (loglevel == LOGLEVEL.LIVE) {
                return true;
            } else if (loglevel == LOGLEVEL.ALL) {
                return false;
            } else if (loglevel == LOGLEVEL.DEBUG) {
                return false;
            } else if (loglevel == LOGLEVEL.INFO) {
                return false;
            } else if (loglevel == LOGLEVEL.WARNING) {
                return true;
            } else if (loglevel == LOGLEVEL.ERROR) {
                return true;
            }
        } else if (this.logSystem.logLevel == LOGLEVEL.ERROR) {
            if (loglevel == LOGLEVEL.LIVE) {
                return true;
            } else if (loglevel == LOGLEVEL.ALL) {
                return false;
            } else if (loglevel == LOGLEVEL.DEBUG) {
                return false;
            } else if (loglevel == LOGLEVEL.INFO) {
                return false;
            } else if (loglevel == LOGLEVEL.WARNING) {
                return false;
            } else if (loglevel == LOGLEVEL.ERROR) {
                return true;
            }
        }
        return false;
    }

}
