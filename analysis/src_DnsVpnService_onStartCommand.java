    @Override // android.app.Service
    public int onStartCommand(Intent intent, int i10, int i11) {
        Command command = intent == null ? Command.START : Command.getEntries().get(intent.getIntExtra("COMMAND", Command.START.ordinal()));
        Log.i("DnsBlockVpn/Service", "Received command - " + command);
        int i12 = b.f18883a[command.ordinal()];
        if (i12 == 1) {
            w();
            x();
        } else if (i12 == 2) {
            y();
        } else if (i12 == 3) {
            r();
        } else {
            if (i12 != 4) {
                throw new NoWhenBranchMatchedException();
            }
            kotlinx.coroutines.j.d(this.f18874b, null, null, new AnonymousClass1(null), 3, null);
        }
        return 1;
    }
