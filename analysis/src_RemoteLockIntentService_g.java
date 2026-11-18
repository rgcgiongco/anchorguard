    @Override // androidx.core.app.h
    protected void g(Intent intent) {
        Intent intent2 = new Intent(intent);
        intent2.setComponent(new ComponentName(getPackageName(), LaunchActivity.class.getName()));
        intent2.setFlags(268435456);
        getApplicationContext().startActivity(intent2);
    }
