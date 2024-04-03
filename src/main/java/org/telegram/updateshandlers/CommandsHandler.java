package org.telegram.updateshandlers;

import lombok.extern.slf4j.Slf4j;
import org.telegram.BotConfig;
import org.telegram.commands.HelloCommand;
import org.telegram.commands.HelpCommand;
import org.telegram.commands.StartCommand;
import org.telegram.commands.StopCommand;
import org.telegram.database.DatabaseManager;
import org.telegram.services.Emoji;
import org.telegram.telegrambots.extensions.bots.commandbot.TelegramLongPollingCommandBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

/**
 * This handler mainly works with commands to demonstrate the Commands feature of the API
 *
 * @author Timo Schulz (Mit0x2)
 */
@Slf4j
public class CommandsHandler extends TelegramLongPollingCommandBot {

    private final String botUsername;

    /**
     * Constructor.
     */
    public CommandsHandler(String botUsername) {
        super();
        this.botUsername = botUsername;
        register(new HelloCommand());
        register(new StartCommand());
        register(new StopCommand());
        HelpCommand helpCommand = new HelpCommand(this);
        register(helpCommand);

        registerDefaultAction((absSender, message) -> {
            SendMessage commandUnknownMessage = new SendMessage();
            commandUnknownMessage.setChatId(Long.toString(message.getChatId()));
            commandUnknownMessage.setText("The command '" + message.getText() + "' is not known by this bot. Here comes some help " + Emoji.AMBULANCE);
            try {
                absSender.execute(commandUnknownMessage);
            } catch (TelegramApiException e) {
                log.error(e.getLocalizedMessage(), e);
            }
            helpCommand.execute(absSender, message.getFrom(), message.getChat(), new String[] {});
        });
    }

    @Override
    public String getBotUsername() {
        return botUsername;
    }

    @Override
    public void processNonCommandUpdate(Update update) {

        if (update.hasMessage()) {
            Message message = update.getMessage();

            if (!DatabaseManager.getInstance().getUserStateForCommandsBot(message.getFrom().getId())) {
                return;
            }

            if (message.hasText()) {
                SendMessage echoMessage = new SendMessage();
                echoMessage.setChatId(Long.toString(message.getChatId()));
                echoMessage.setText("Hey heres your message:\n" + message.getText());

                try {
                    execute(echoMessage);
                } catch (TelegramApiException e) {
                    log.error(e.getLocalizedMessage(), e);
                }
            }
        }
    }

    @Override
    public String getBotToken() {
        return BotConfig.COMMANDS_TOKEN;
    }
}