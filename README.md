# ΩConfig

---

*The last config library you will ever use.*

ΩConfig is a hyper-minimal config library based on [Auto Config](https://github.com/shedaniel/AutoConfig). It aims to
achieve the following goals:

- Be lightweight (<25 KB) for JIJ usage
- Exceedingly simple design & API for developers
- Intuition and usability for players
- Bonus annotations for advanced config options (syncing values)

The following is an example of a simple ΩConfig setup:

```java
public class TestConfig implements Config {

    @Comment(value = "Hello!")
    boolean value = false;

    @Override
    public String getFileName() {
        return "test-config";
    }
}

```

```java
public class MyModInitializer {

    public static final TestConfig CONFIG = OmegaConfig.register(TestConfig.class);

    @Override
    public void onInitialize() {
        System.out.printf("Config value: %s%n", CONFIG.value);
    }
}
```

Looking for a simple config screen? Talk about easy!
```java
public class ClientInitializer implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        // Make sure you implement getModid in your config class!
        OmegaConfigGui.registerConfigScreen(MainInitializer.CONFIG);
    }
}
```

---

### Pulling Omega Config into Development

To use Omega Config, you will have to add it to your build.gradle file.

What you pull in depends on whether you want GUI functionality. For basic config files using the base module (~20KB),
you can use the following gradle declarations:

```groovy
repositories {
    maven { url 'https://jitpack.io' }
}

dependencies {
    include("com.github.Draylar.omega-config:omega-config-base:${project.omega_config_version}")
    modImplementation("com.github.Draylar.omega-config:omega-config-base:${project.omega_config_version}")
}
```

Easy - you now have a bundled configuration library. Use the examples in the first section to implement your config.

If you want to add GUI functionality (most likely Mod Menu support), you can pull in the GUI module (~25 KB):
```groovy
repositories {
    ...

    // Needed to retrieve Cloth Config Lite for Omega Config in development environments.
    maven {
        name = "Shedaniel's Maven"
        url = "https://maven.shedaniel.me/"
    }

    // Optional dependency for Mod Menu - recommended for viewing your screen in development
    maven {
        name = "TerraformersMC"
        url = "https://maven.terraformersmc.com/releases/"
    }
}

dependencies {
    ... (including the base declarations)
    
    include("com.github.Draylar.omega-config:omega-config-gui:${project.omega_config_version}")
    modImplementation("com.github.Draylar.omega-config:omega-config-gui:${project.omega_config_version}")
    modRuntimeOnly ("com.terraformersmc:modmenu:${project.modmenu_version}") // 3.0.1 for 1.18.1
}
```

---

### Extra API Utilities

ΩConfig provides several utility methods for developers.

**save()** - *saves a modified configuration instance to disk*

```java
MyModInitializer.CONFIG.value=false;
        MyModInitializer.CONFIG.save(); // writes the new value to disk
```


**@Syncing** - *configuration options marked with this annotation will automatically sync to the client when they join a server.*

---

### License

ΩConfig is available under MIT. Omega Config will bundle the MIT license inside the jar you pull as a dependency, which means you can distribute it as a bundled dependency without any additional steps.