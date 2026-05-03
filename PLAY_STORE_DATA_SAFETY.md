# Play Store — Data Safety Form Answers
# Play Console → App content → Data safety

## Does your app collect or share any of the required user data types?
**YES** (see details below)

---

## Data Types

### Phone number
- **Collected:** YES — incoming caller phone numbers are checked against local block list
- **Shared with third parties:** YES (Premium only) — sent to IPQS for spam scoring
- **Required / Optional:** Optional (IPQS feature is opt-in Premium)
- **Purpose:** App functionality (fraud detection)
- **Encrypted in transit:** YES
- **User can request deletion:** YES (uninstall app clears all local data)

### SMS messages
- **Collected:** NO — SMS content is processed locally and never stored or transmitted
- **Shared:** NO

### Call logs
- **Collected:** NO — call history is accessed locally for screening, never stored beyond the session
- **Shared:** NO

---

## Security practices
- [x] Data is encrypted in transit (HTTPS only, see network_security_config.xml)
- [x] You provide a way for users to request that their data be deleted (uninstall)
- [ ] Your app follows the Families Policy  (N/A — not targeting children)

---

## Privacy Policy URL
https://callshield.app/privacy   ← Replace with your hosted URL before submission
(or link directly to the in-app screen)

---

## Notes for reviewer
- The app uses READ_SMS and RECEIVE_SMS to detect spam locally.
  No SMS text ever leaves the device.
- READ_CALL_LOG and ANSWER_PHONE_CALLS are used only for real-time
  call screening via Android's CallScreeningService API.
- IPQS (ipqualityscore.com) is a third-party service used ONLY for
  Premium subscribers who explicitly opt in. Only the caller's phone
  number is sent; no other data.
- All local data (block list, events) is stored in Room on-device.
  Cloud backup is disabled (see data_extraction_rules.xml).
