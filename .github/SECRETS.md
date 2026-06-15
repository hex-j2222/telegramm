# Required GitHub Secrets

Go to: Settings → Secrets and variables → Actions → New repository secret

| Secret Name  | Description                          | Example           |
|-------------|--------------------------------------|-------------------|
| `TG_API_ID`  | Your Telegram App ID from my.telegram.org | `12345678`   |
| `TG_API_HASH`| Your Telegram App Hash               | `abc123def456...` |

## How to get API credentials

1. Open https://my.telegram.org/apps
2. Sign in with your phone number
3. Click "API Development Tools"
4. Create a new application
5. Copy `api_id` and `api_hash`

## Important Notes

- NEVER commit `local.properties` to Git
- The `.gitignore` already excludes it
- Secrets are injected at build time only
- APK release builds use ProGuard/R8 to obfuscate the values
