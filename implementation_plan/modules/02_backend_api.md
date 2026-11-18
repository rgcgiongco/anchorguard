# Module 02 — Backend API (Self-Hosted + PostgreSQL)

## Overview
Own REST API running in Docker, backed by PostgreSQL. Parent web console and Android client both consume it. No Firebase dependency in MVP (push deferred; polling or WebSocket can be added later).

## Tech Stack
- Language: Kotlin or Java 21
- Framework: Spring Boot 3.x or Ktor
- Database: PostgreSQL 15+
- Migrations: Flyway
- Auth: JWT (access + refresh tokens), bcrypt/argon2 for passwords
- API style: REST, JSON, OpenAPI/Swagger docs
- Container: Docker, Docker Compose

## Base URL (example, choose your own)
`https://api.anchorguard.com/v1`

## Endpoints (design your own paths, do not copy reference)

### Auth
| Method | Path | Description |
|---|---|---|
| POST | `/auth/register` | Parent account creation (email, password, name) |
| POST | `/auth/login` | Email/password login → access + refresh token |
| POST | `/auth/refresh` | Rotate access token |
| POST | `/auth/logout` | Invalidate refresh token |
| POST | `/auth/password-reset/request` | Send reset token to email |
| POST | `/auth/password-reset/confirm` | Set new password with token |

### Devices
| Method | Path | Description |
|---|---|---|
| POST | `/devices/register` | Child device registers, returns device ID + pairing code |
| POST | `/devices/pair` | Parent submits pairing code to link device to account |
| GET | `/devices` | List parent's linked devices |
| GET | `/devices/{id}` | Device detail (name, model, last seen, status) |
| PATCH | `/devices/{id}` | Update device name, settings |
| DELETE | `/devices/{id}` | Unregister device |
| POST | `/devices/{id}/heartbeat` | Device check-in (last seen, IP, app version) |

### Policy / Configuration
| Method | Path | Description |
|---|---|---|
| GET | `/devices/{id}/policy` | Full device policy (apps, limits, schedules) |
| PUT | `/devices/{id}/policy` | Replace policy |
| PATCH | `/devices/{id}/policy` | Partial policy update |
| GET | `/devices/{id}/apps` | Installed app catalogue with allow/block state |
| PATCH | `/devices/{id}/apps/{package}` | Set app allowed/blocked, time limit |
| GET | `/devices/{id}/schedule` | Weekly block schedule grid |
| PUT | `/devices/{id}/schedule` | Set schedule |

### Reports / Usage
| Method | Path | Description |
|---|---|---|
| POST | `/reports/usage` | Ingest app launch / screen time events from device |
| GET | `/reports/usage` | Query usage aggregates (range, device, app) |
| GET | `/reports/export` | CSV export of usage (deferred) |

### Account
| Method | Path | Description |
|---|---|---|
| GET | `/account/me` | Parent profile |
| PATCH | `/account/me` | Update profile |
| POST | `/account/change-password` | Authenticated password change |
| DELETE | `/account/me` | Account deletion (GDPR-style) |

## Database Schema (PostgreSQL)
```sql
-- parents
CREATE TABLE parents (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  email TEXT UNIQUE NOT NULL,
  password_hash TEXT NOT NULL,
  name TEXT,
  email_verified BOOLEAN DEFAULT FALSE,
  created_at TIMESTAMPTZ DEFAULT NOW(),
  updated_at TIMESTAMPTZ DEFAULT NOW()
);

-- devices
CREATE TABLE devices (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  hex_id TEXT UNIQUE NOT NULL,       -- friendly device identifier for URLs
  parent_id UUID REFERENCES parents(id),
  name TEXT,
  model TEXT,
  manufacturer TEXT,
  android_id TEXT,
  app_version TEXT,
  last_seen TIMESTAMPTZ,
  is_online BOOLEAN DEFAULT FALSE,
  pairing_code TEXT,                  -- short code for parent-to-device linking
  pairing_expires_at TIMESTAMPTZ,
  created_at TIMESTAMPTZ DEFAULT NOW()
);

-- app_policies
CREATE TABLE app_policies (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  device_id UUID REFERENCES devices(id) ON DELETE CASCADE,
  package_name TEXT NOT NULL,
  app_name TEXT,
  is_allowed BOOLEAN DEFAULT TRUE,
  daily_limit_minutes INTEGER,        -- NULL = unrestricted
  block_schedule JSONB,               -- { "mon": [[start,end],...], ... }
  internet_access TEXT DEFAULT 'on',  -- on|off|custom
  created_at TIMESTAMPTZ DEFAULT NOW(),
  updated_at TIMESTAMPTZ DEFAULT NOW()
);

-- device_settings
CREATE TABLE device_settings (
  device_id UUID PRIMARY KEY REFERENCES devices(id) ON DELETE CASCADE,
  advanced_timer BOOLEAN DEFAULT FALSE,
  background_mode BOOLEAN DEFAULT FALSE,
  tamper_proof BOOLEAN DEFAULT FALSE,
  boot_start BOOLEAN DEFAULT FALSE,
  keep_alive BOOLEAN DEFAULT FALSE,
  brute_force_pin BOOLEAN DEFAULT FALSE,
  block_interval_ms INTEGER DEFAULT 500,
  app_title TEXT DEFAULT 'AnchorGuard',
  updated_at TIMESTAMPTZ DEFAULT NOW()
);

-- usage_events
CREATE TABLE usage_events (
  id BIGSERIAL PRIMARY KEY,
  device_id UUID REFERENCES devices(id) ON DELETE CASCADE,
  package_name TEXT NOT NULL,
  event_type TEXT NOT NULL,           -- launch, foreground, background, block
  occurred_at TIMESTAMPTZ DEFAULT NOW()
);

-- users (profiles on device)
CREATE TABLE device_users (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  device_id UUID REFERENCES devices(id) ON DELETE CASCADE,
  name TEXT NOT NULL,
  pin_hash TEXT,                      -- optional per-user PIN
  created_at TIMESTAMPTZ DEFAULT NOW()
);
```

## Security
- HTTPS only (Let's Encrypt via Nginx or cert-manager)
- JWT access token short-lived (15 min), refresh token longer (7 days)
- Rate limiting on auth endpoints
- CORS restricted to `app.anchorguard.com` and Android client origin
- No secrets in client code; API keys provisioned at device registration

## Deployment
- Docker Compose for single-server MVP
- PostgreSQL volume for persistence
- Nginx reverse proxy + SSL termination
- Health check endpoint: `/health`
