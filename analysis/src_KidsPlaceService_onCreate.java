    @Override // android.app.Service
    public void onCreate() {
        super.onCreate();
        Utility.i4("KP_TRACE onCreate", "KidsHomeService");
        i0();
        int iF0 = Utility.F0(this);
        this.f19105a = iF0;
        this.Q = iF0 * (this.R + 3);
        this.W = new ArrayList();
        f19100s0 = com.kiddoware.kidsplace.i.b(getApplicationContext());
        this.H = Utility.h();
        this.X = f19100s0.o().u();
        ud.v vVar = this.U;
        if (vVar != null) {
            vVar.g();
        }
        this.U = new ud.v(this);
        this.V = new ud.c(this);
        new c1(getApplicationContext()).execute(-1);
        Q();
        c0(false);
        f19103v0 = KPEventsManager.e(getApplication());
        try {
            f1.g(getApplicationContext());
            this.f19107b = (ActivityManager) getSystemService("activity");
            getPackageManager();
            p();
            S();
            try {
                this.C = ((LayoutInflater) getSystemService("layout_inflater")).inflate(R.layout.block_popup, (ViewGroup) null);
            } catch (Exception e10) {
                Utility.j4("onCreate:Inflating Toast Layout", "KidsHomeService", e10);
                this.C = null;
            }
        } catch (Exception e11) {
            Utility.j4("onCreate", "KidsHomeService", e11);
            c0(true);
            stopSelf();
        }
    }
