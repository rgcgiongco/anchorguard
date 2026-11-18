    @Override // android.app.Service
    public int onStartCommand(Intent intent, int i10, int i11) {
        Utility.l4("onStartCommand", "KidsHomeService");
        i0();
        try {
            if (com.kiddoware.kidsplace.i.w()) {
                Utility.l4("onStartCommand::isExiting", "KidsHomeService");
                c0(true);
                stopSelf();
                return 2;
            }
            boolean z10 = false;
            c0(false);
            boolean z11 = intent == null;
            boolean z12 = intent != null && intent.hasExtra("ScreenReceiver");
            Utility.i4("KP_TRACE onStartCommand: stickyRestart=" + z11 + " checkForScreenReceiver=" + z12 + " isServiceRunning=" + I() + " screenOn=" + f19097p0 + " deviceUnlocked=" + f19099r0, "KidsHomeService");
            if (f19097p0 && f19099r0 && !z12) {
                Utility.i4("KP_TRACE onStartCommand: branch=active (start + ensure watchdog)", "KidsHomeService");
                if (!I()) {
                    m0();
                    Utility.l4("onStartCommand::Setting KP Service Running flag screenOn", "KidsHomeService");
                    d0(true);
                }
                try {
                    y(intent);
                } catch (Exception e10) {
                    e10.printStackTrace();
                    Utility.j4("onStartCommand::startForegroundService1", "KidsHomeService", e10);
                }
            } else {
                boolean z13 = intent != null && intent.getBooleanExtra("ScreenReceiver", false);
                if (intent != null && intent.getBooleanExtra("DeviceUnlockedReceiver", false)) {
                    z10 = true;
                }
                if (z13) {
                    N(intent);
                } else if (z10) {
                    f19099r0 = true;
                    f19097p0 = true;
                    Utility.l4("fromDeviceUnlockedReciever deviceUnlocked ::" + f19099r0, "KidsHomeService");
                    if (!I()) {
                        try {
                            d0(true);
                            y(intent);
                        } catch (Exception e11) {
                            e11.printStackTrace();
                            Utility.j4("onStartCommand::startForegroundService3", "KidsHomeService", e11);
                        }
                    }
                }
            }
            if (Utility.n3(getApplicationContext())) {
                this.P.b(this);
            }
            oe.f.i(this).w();
        } catch (Exception e12) {
            Utility.j4("onStartCommand", "KidsHomeService", e12);
        }
        return 1;
    }
