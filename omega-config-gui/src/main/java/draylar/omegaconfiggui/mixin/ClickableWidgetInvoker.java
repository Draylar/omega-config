package draylar.omegaconfiggui.mixin;

import net.minecraft.client.gui.widget.ClickableWidget;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(ClickableWidget.class)
public interface ClickableWidgetInvoker {
    @Invoker
    void callSetFocused(boolean focused);
}
