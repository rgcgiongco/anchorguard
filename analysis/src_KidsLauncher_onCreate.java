    /* JADX WARN: Code duplicated, block: B:71:0x0372  */
    /* JADX WARN: Code duplicated, block: B:80:0x0262 A[EXC_TOP_SPLITTER, SYNTHETIC] */
    /* JADX WARN: Code duplicated, block: B:83:0x0247 A[EXC_TOP_SPLITTER, SYNTHETIC] */
    /* JADX WARN: Code duplicated, block: B:87:? A[RETURN, SYNTHETIC] */
    @Override // android.app.Application
    public void onCreate() {
        bf.a aVar;
        super.onCreate();
        try {
            f19022u = this;
            f19023v = getApplicationContext();
            androidx.lifecycle.i0.m().d().a(new a());
            androidx.appcompat.app.g.M(1);
            NotificationManager notificationManager = (NotificationManager) getSystemService("notification");
            if (notificationManager.getNotificationChannel("DEFAULT") == null) {
                String string = getString(R.string.home_title);
                String string2 = getString(R.string.notification_channecl_desc);
                NotificationChannel notificationChannel = new NotificationChannel("DEFAULT", string, 3);
                notificationChannel.setSound(null, null);
                notificationChannel.setDescription(string2);
                notificationManager.createNotificationChannel(notificationChannel);
            }
            if (Utility.K3(this) && !Utility.L3(this)) {
                Utility.n7(this, false);
                Utility.o7(this, true);
            }
            g1.a(this);
            ef.d.g(getApplicationContext());
            new he.c().a(getApplicationContext());
            if (Utility.P() == null) {
                Utility.G4(getApplicationContext());
            }
            this.f19032e = new bf.a();
            KidsPlaceRepository kidsPlaceRepository = new KidsPlaceRepository(this);
            this.f19038t = kidsPlaceRepository;
            kidsPlaceRepository.d0(w().getId());
            this.f19032e.c().execute(new Runnable() { // from class: com.kiddoware.kidsplace.s
                @Override // java.lang.Runnable
                public final void run() {
                    this.f20797a.x();
                }
            });
            Thread.currentThread().setUncaughtExceptionHandler(new v0(getApplicationContext()));
            Utility.v6(getApplicationContext());
            le.i.B(getApplicationContext());
            i iVarB = i.b(getApplicationContext());
            this.f19036r = iVarB;
            iVarB.P(this);
            ud.j.a().b(getApplicationContext());
            this.f19029b = new ArrayList();
            this.f19030c = new ArrayList();
            this.f19031d = new ArrayList();
            PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
            SQLiteDatabase sQLiteDatabaseU = u();
            int iF = F(sQLiteDatabaseU);
            if (!Utility.q(getApplicationContext()) || iF == 0) {
                Resources resources = getApplicationContext().getResources();
                Category[] categoryArr = {new Category(-2L, 0, resources.getString(R.string.app_category_default_no_category), false), new Category(0L, -2592945, resources.getString(R.string.app_category_default_maths), true), new Category(1L, -9110, resources.getString(R.string.app_category_default_music), true), new Category(2L, -6368089, resources.getString(R.string.app_category_default_videos), true), new Category(3L, -9523221, resources.getString(R.string.app_category_default_storybooks), true), new Category(4L, -4826709, resources.getString(R.string.app_category_default_games), true), new Category(5L, -11843775, resources.getString(R.string.app_category_default_language), true), new Category(6L, -9509379, resources.getString(R.string.app_category_default_art), true)};
                new Random();
                Category category = categoryArr[0];
                category.setIconResourceIndex(f19024w[1]);
                category.insert(sQLiteDatabaseU);
                this.f19029b.add(category);
                Utility.T4(getApplicationContext(), true);
            }
            try {
                if (!Utility.Q2(getApplicationContext()) || User.GetUsersCount(sQLiteDatabaseU) < 1) {
                    try {
                        User userGetDefaultUser = User.GetDefaultUser();
                        userGetDefaultUser.setName(getApplicationContext().getResources().getString(R.string.default_user_name));
                        userGetDefaultUser.insert(u());
                        Utility.Y4(getApplicationContext(), true);
                    } catch (Exception e10) {
                        Utility.j4("SETUP Default User", "KidsLauncher", e10);
                    }
                } else {
                    I(w());
                }
            } catch (Exception unused) {
            }
            try {
                if (Utility.s(getApplicationContext())) {
                    if (Utility.t(getApplicationContext())) {
                        new c().execute(null, null, null);
                        if (!Utility.j3(getApplicationContext())) {
                            this.f19032e.a().execute(new Runnable() { // from class: com.kiddoware.kidsplace.t
                                @Override // java.lang.Runnable
                                public final void run() {
                                    this.f20951a.y();
                                }
                            });
                            Utility.E5(getApplicationContext(), true);
                        }
                    } else {
                        new c().execute(null, null, null);
                        if (!Utility.j3(getApplicationContext())) {
                            this.f19032e.a().execute(new Runnable() { // from class: com.kiddoware.kidsplace.t
                                @Override // java.lang.Runnable
                                public final void run() {
                                    this.f20951a.y();
                                }
                            });
                            Utility.E5(getApplicationContext(), true);
                        }
                    }
                    aVar = this.f19032e;
                    if (aVar != null) {
                        aVar.b().execute(new Runnable() { // from class: com.kiddoware.kidsplace.x
                            @Override // java.lang.Runnable
                            public final void run() {
                                this.f21253a.C();
                            }
                        });
                    }
                }
                try {
                    new b().execute(null, null, null);
                } catch (Exception e11) {
                    Utility.j4("SETUP pre-installs apps", "KidsLauncher", e11);
                }
                if (Utility.t(getApplicationContext()) || !Utility.u(getApplicationContext())) {
                    try {
                        new c().execute(null, null, null);
                    } catch (Exception e12) {
                        Utility.j4("SETUP pre-installs apps", "KidsLauncher", e12);
                    }
                    if (!Utility.j3(getApplicationContext())) {
                        try {
                            this.f19032e.a().execute(new Runnable() { // from class: com.kiddoware.kidsplace.t
                                @Override // java.lang.Runnable
                                public final void run() {
                                    this.f20951a.y();
                                }
                            });
                            Utility.E5(getApplicationContext(), true);
                        } catch (Exception e13) {
                            Utility.j4("SETUP internet block reset", "KidsLauncher", e13);
                        }
                    }
                } else if (!Utility.j3(getApplicationContext())) {
                    this.f19032e.a().execute(new Runnable() { // from class: com.kiddoware.kidsplace.t
                        @Override // java.lang.Runnable
                        public final void run() {
                            this.f20951a.y();
                        }
                    });
                    Utility.E5(getApplicationContext(), true);
                }
            } catch (Exception unused2) {
            }
            n();
            Utility.f19225l = String.valueOf((int) (getApplicationContext().getResources().getDimension(R.dimen.app_grid_item) / getApplicationContext().getResources().getDisplayMetrics().density));
            Utility.f19226m = String.valueOf((int) (getApplicationContext().getResources().getDimension(R.dimen.app_parent_size) / getApplicationContext().getResources().getDisplayMetrics().density));
            Utility.R5(getApplicationContext());
            Utility.o4(getApplicationContext());
            p().c().execute(new Runnable() { // from class: com.kiddoware.kidsplace.u
                @Override // java.lang.Runnable
                public final void run() {
                    this.f21024a.z();
                }
            });
            if (Utility.A1(this, Utility.PrivacySettings.analytics)) {
                this.f19032e.b().execute(new Runnable() { // from class: com.kiddoware.kidsplace.v
                    @Override // java.lang.Runnable
                    public final void run() {
                        this.f21071a.A();
                    }
                });
            }
            if (Utility.A1(this, Utility.PrivacySettings.crashReporting)) {
                com.google.firebase.crashlytics.a.a().e(true);
            }
            if (i.f20260j < 23) {
                bf.s.l(this);
            }
            AsyncTask.execute(new Runnable() { // from class: com.kiddoware.kidsplace.w
                @Override // java.lang.Runnable
                public final void run() {
                    this.f21204a.B();
                }
            });
            if (Utility.H3(this)) {
                Utility.U7(this);
                if (Utility.b3(getApplicationContext())) {
                    Utility.X6(getApplicationContext(), System.currentTimeMillis());
                    Utility.q5(getApplicationContext(), false);
                }
            }
            new k(getApplicationContext(), getPackageName()).execute(null, null, null);
            new te.b(getApplicationContext()).execute(null, null, null);
        } catch (Exception e14) {
            Utility.j4("App:onCreate()", "KidsLauncher", e14);
        }
        aVar = this.f19032e;
        if (aVar != null) {
            aVar.b().execute(new Runnable() { // from class: com.kiddoware.kidsplace.x
                @Override // java.lang.Runnable
                public final void run() {
                    this.f21253a.C();
                }
            });
        }
    }
