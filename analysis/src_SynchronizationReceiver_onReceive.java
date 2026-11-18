    @Override // android.content.BroadcastReceiver
    public void onReceive(Context context, Intent intent) {
        this.f20758b = context;
        if (intent.getAction().equals(this.f20757a)) {
            this.f20759c.post(this.f20760d);
        }
        try {
            if (l0.E(context)) {
                l0.Z(context);
            }
        } catch (Exception unused) {
        }
    }
