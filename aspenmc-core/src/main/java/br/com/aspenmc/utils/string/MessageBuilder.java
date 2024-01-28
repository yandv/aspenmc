package br.com.aspenmc.utils.string;

import lombok.Getter;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;

import java.util.ArrayList;
import java.util.List;

/**
 * Assist you to create a TextComponent
 *
 * @author yandv
 */

@Getter
public class MessageBuilder {

    private List<TextComponent> componentList;

    public MessageBuilder(String message) {
        this.componentList = new ArrayList<>();
        this.componentList.add(new TextComponent(message));
    }

    public MessageBuilder setMessage(String message) {
        this.componentList.set(0, new TextComponent(message));
        return this;
    }

    public MessageBuilder setHoverEvent(HoverEvent.Action action, String text) {
        TextComponent textComponent = this.componentList.get(0);
        textComponent.setHoverEvent(new HoverEvent(action, TextComponent.fromLegacyText(text)));
        return this;
    }

    public MessageBuilder setHoverEvent(String text) {
        return setHoverEvent(HoverEvent.Action.SHOW_TEXT, text);
    }

    public MessageBuilder setClickEvent(String text) {
        return setClickEvent(ClickEvent.Action.RUN_COMMAND, text);
    }

    public MessageBuilder setClickEvent(ClickEvent.Action action, String text) {
        TextComponent textComponent = this.componentList.get(0);
        textComponent.setClickEvent(new ClickEvent(action, text));
        return this;
    }

    public MessageBuilder append(String message) {
        this.componentList.add(new TextComponent(message));
        return this;
    }

    public MessageBuilder append(String message, String clickableText, String hoverText) {
        TextComponent textComponent = new TextComponent(message);

        if (!clickableText.isEmpty()) {
            textComponent.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, clickableText));
        }

        if (!hoverText.isEmpty()) {
            textComponent.setHoverEvent(
                    new HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText(hoverText)));
        }

        this.componentList.add(textComponent);
        return this;
    }

    public MessageBuilder append(String message, String hoverText) {
        return append(message, "", hoverText);
    }

    public MessageBuilder append(MessageBuilder messageBuilder) {
        this.componentList.add(messageBuilder.create());
        return this;
    }

    public MessageBuilder append(TextComponent textComponent) {
        this.componentList.add(textComponent);
        return this;
    }

    public MessageBuilder append(List<TextComponent> extra) {
        this.componentList.addAll(extra);
        return this;
    }

    public TextComponent create() {
        TextComponent textComponent = new TextComponent("");

        for (TextComponent text : componentList)
            textComponent.addExtra(text);

        return textComponent;
    }
}
