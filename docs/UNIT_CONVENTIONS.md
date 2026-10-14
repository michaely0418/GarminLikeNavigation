# Unit / type conventions

- WGS84 geolocation: `GeoPoint.latitudeDeg`, `GeoPoint.longitudeDeg`, both finite Doubles.
- Length: `distanceMeters: Double`, finite nonnegative; present kilometers only in HUD.
- Speed: `speedMps: Double`, finite nonnegative; convert for display with `kmh = mps * 3.6`.
- Heading: `bearingDeg: Double` within `[0,360)`, clockwise from true north, if unknown use null.
- GPS accuracy: `horizontalAccuracyMeters: Double?`, never infer missing as zero.
- Timestamp: `epochMillis: Long` UTC Unix milliseconds; event expiration: `expiresAtEpochMillis: Long?`.
- Duration: `durationSeconds: Long` nonnegative.
- Unknown legality: `RoadAccessStatus.UNKNOWN`, do not map to permitted route.
- Vehicle enum: `CAR`, `SCOOTER`, `HEAVY_MOTORCYCLE`. Heavy-motorcycle legal eligibility may require vehicle attributes and segment-specific restriction data before Stage 3.
- Interop payloads / map-matching quality and low-accuracy GPS must be explicitly defined when implemented.
