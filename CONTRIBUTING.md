# Contributing to BroadcastSim

Use Java 21 and Maven for all core-module changes. Keep `broadcastsim-core` free of Spring dependencies and implement only the active milestone.

Before submitting a change, run:

```powershell
mvn -f broadcastsim-core\pom.xml verify
```

The build runs unit tests, code-style checks, formatting checks, and JaCoCo reporting. Follow the existing package structure and add JavaDoc to public classes.
