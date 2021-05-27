# ΩConfig

---

*The last config library you will ever use.*

ΩConfig is a hyper-minimal config library based on [Auto Config](https://github.com/shedaniel/AutoConfig). It aims to
achieve the following goals:

- Be lightweight (<10 KB) for JIJ usage
- Exceedingly simple design & API for developers
- Intuition and usability for players

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

---

### Extra API Utilities

ΩConfig provides several utility methods for developers.

**save()** - *saves a modified configuration instance to disk*

```java
MyModInitializer.CONFIG.value=false;
        MyModInitializer.CONFIG.save(); // writes the new value to disk
```

---

### License

ΩConfig is available under Public Domain. You are encouraged to utilize the code in this repository in any way you wish.