package draylar.omegaconfiggui.mixin;

import net.minecraft.client.gui.widget.AbstractButtonWidget;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(AbstractButtonWidget.class)
public interface AbstractButtonWidgetAccessor {
    @Invoker
    void callSetFocused(boolean focused);
}
