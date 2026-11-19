# Module 01 — Dashboard Stat Cards

## Goal
Implement the top stat cards row showing Active Devices, Today's Screen Time, Blocked Attempts, and Current Status.

## Tasks
1. Create stat card component with label, value, subtitle, and color-coded icon
2. Arrange 4 stat cards in responsive grid (1 col mobile, 2 col tablet, 4 col desktop)
3. Card 1: Active Devices — device count, "X online" subtitle, navy color
4. Card 2: Today's Screen Time — "3h 12m" value, "+12 min vs yesterday" subtitle, teal color
5. Card 3: Blocked Attempts — "14" value, "Last 24 hours" subtitle, orange color
6. Card 4: Current Status — "Secured" value, "All devices protected" subtitle, green color
7. Connect stat values to live device data from `devicesApi.list()`
8. Add refresh button to reload dashboard data

## Files
- `src/pages/Dashboard.tsx` — StatCard component and stat grid layout

## Deliverable
Stat cards row renders with correct values, colors, and responsive layout.
