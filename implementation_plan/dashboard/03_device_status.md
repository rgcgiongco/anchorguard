# Module 03 — Device Status

## Goal
Implement the Device Status section with individual device cards showing screen time, active app, and time limit progress bars.

## Tasks
1. Create device card component with device icon, name, online/offline indicator
2. Each card shows: Screen Time Today (min + progress bar), Active App (progress bar), Time Limit (progress bar)
3. Display "No devices linked" empty state when devices array is empty
4. Responsive grid: 1 col mobile, 2 col tablet, 3 col desktop
5. Device icons: tablet, phone, iPad (inline SVGs)
6. Progress bars use brand colors (navy, teal, orange)
7. Connect to `devicesApi.list()` for live device data

## Files
- `src/pages/Dashboard.tsx` — DeviceStatus, DeviceCard components

## Deliverable
Device status cards render with progress bars, online/offline indicators, and empty state.
