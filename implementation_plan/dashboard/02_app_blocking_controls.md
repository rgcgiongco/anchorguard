# Module 02 — App Blocking & Controls

## Goal
Implement the App Blocking & Controls panel with toggle switches for app categories.

## Tasks
1. Create toggle switch component with checked/unchecked states and smooth animation
2. Categories: Social Apps, Chat, Gaming, Learning (each with color-coded icon badge)
3. Default states: Social Apps (on), Chat (off), Gaming (on), Learning (off)
4. Toggle click updates component state and reflects visual change immediately
5. Connect toggles to backend policy via `policyApi.get()` and `policyApi.patch()`
6. Display "Active" badge in panel header
7. Handle API errors gracefully (fall back to local state)

## Files
- `src/pages/Dashboard.tsx` — AppBlockingControls component, toggle switch

## Deliverable
Toggle switches render correctly, respond to clicks, and sync with backend policy.
