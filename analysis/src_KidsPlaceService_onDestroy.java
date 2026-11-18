    @Override // android.app.Service
    public void onDestroy() {
        super.onDestroy();
        try {
            ud.v vVar = this.U;
            if (vVar != null) {
                vVar.g();
                this.U = null;
            }
        } catch (Exception e10) {
            Utility.j4("onDestroy:timerPopup", "KidsHomeService", e10);
        }
        try {
            this.f19121s.cancel();
            this.F = false;
            d0(false);
            V();
            r1 r1Var = this.f19123u;
            r1Var.f20727d = null;
            unregisterReceiver(r1Var);
            unregisterReceiver(this.f19124v);
            unregisterReceiver(this.f19125w);
            unregisterReceiver(this.f19126x);
            unregisterReceiver(this.f19122t);
            unregisterReceiver(this.A);
            n0();
            te.r.d().f(this.f19127y);
            te.q.d().f(this.f19128z);
            this.B.b(this);
            ud.c cVar = this.V;
            if (cVar != null) {
                cVar.f();
            }
            oe.f.i(this).x();
            this.Y.g();
            Utility.i4("KP_TRACE onDestroy:Done", "KidsHomeService");
            k0();
        } catch (Exception e11) {
            Utility.j4("onDestrory", "KidsHomeService", e11);
        }
    }
