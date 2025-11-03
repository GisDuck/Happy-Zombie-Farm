package game.Happy_Zombie_Farm.service;

import game.Happy_Zombie_Farm.dto.TelegramAuthDto;
import game.Happy_Zombie_Farm.dto.outputDto.AuthPayloadDto;
import game.Happy_Zombie_Farm.entity.Player;
import game.Happy_Zombie_Farm.entity.UserAuth;
import game.Happy_Zombie_Farm.enums.BoardColor;
import game.Happy_Zombie_Farm.mapper.PlayerMapper;
import game.Happy_Zombie_Farm.repository.PlayerRepository;
import game.Happy_Zombie_Farm.repository.UserAuthRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ThreadLocalRandom;

import static java.nio.charset.StandardCharsets.UTF_8;

@Service
public class TelegramAuthService {
    @Autowired
    private UserAuthRepository userAuthRepository;
    @Autowired
    private PlayerRepository playerRepository;
    @Autowired
    private PlayerMapper playerMapper;

    private static final Logger log = LoggerFactory.getLogger(TelegramAuthService.class);

    @Value("${TELEGRAM_BOT_TOKEN:}")
    private String tgBotToken;

    /**
     * проверяет данные, полученные из телеграм
     */
    public boolean telegramDataIsValid(TelegramAuthDto telegramAuthDto) {
        //получаем хэш, который позже будем сравнивать с остальными данными
        String hash = telegramAuthDto.hash();

        log.info("user telegram data {}", telegramAuthDto);
        log.info("user hash {}", hash);
        log.info("tgBot token {}", tgBotToken);

        Map<String, String> data = getTelegramAuthMap(telegramAuthDto);

        //создаем строку проверки - сортируем все параметры и объединяем их в строку вида:
        //auth_date=<auth_date>\nfirst_name=<first_name>\nid=<id>\nusername=<username>
        StringBuilder sb = new StringBuilder();
        data.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .forEach(entry -> sb.append(entry.getKey()).append("=").append(entry.getValue()).append("\n"));
        sb.deleteCharAt(sb.length() - 1);
        String dataCheckString = sb.toString();

        try {
            //генерируем SHA-256 хэш из токена бота
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] key = digest.digest(tgBotToken.getBytes(UTF_8));

            log.info("tgBotHash {}", key.toString());

            //создаем HMAC со сгенерированным хэшем
            Mac hmac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKeySpec = new SecretKeySpec(key, "HmacSHA256");
            hmac.init(secretKeySpec);

            // добавляем в HMAC строку проверки и переводим в шестнадцатеричный формат
            byte[] hmacBytes = hmac.doFinal(dataCheckString.getBytes(UTF_8));
            StringBuilder validateHash = new StringBuilder();
            for (byte b : hmacBytes) {
                validateHash.append(String.format("%02x", b));
            }

            log.info("validateHash {}", validateHash);

            // сравниваем полученный от телеграма и сгенерированный хэш
            return hash.contentEquals(validateHash);
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            throw new RuntimeException(e);
        }
    }

    @Transactional
    public Player registerTelegramUser(TelegramAuthDto telegramAuthDto) {

        Instant now = Instant.now();

        Player player = new Player();
        player.setUsername(telegramAuthDto.username());
        player.setPhotoUrl(telegramAuthDto.photoUrl());
        player.setBoardColor(getRandomBoardColor());
        player.setMeat(0L);
        player.setGold(0L);
        player.setBrain(0L);
        player.setLastMeatUpdate(now);
        player = playerRepository.save(player);

        UserAuth ua = new UserAuth();
        ua.setTelegramId(telegramAuthDto.id());
        ua.setPlayer(player);
        ua.setCreatedAt(now);
        userAuthRepository.save(ua);

        return player;
    }

    private static BoardColor getRandomBoardColor() {
        BoardColor[] values = BoardColor.values();
        int idx = ThreadLocalRandom.current().nextInt(values.length);
        return values[idx];
    }

    private static Map<String, String> getTelegramAuthMap(TelegramAuthDto telegramAuthDto) {
        Map<String, String> data = new TreeMap<>();

        if (telegramAuthDto.authDate() != null) {
            data.put("auth_date", telegramAuthDto.authDate().toString());
        }
        if (telegramAuthDto.firstName() != null) {
            data.put("first_name", telegramAuthDto.firstName());
        }
        if (telegramAuthDto.lastName() != null) {
            data.put("last_name", telegramAuthDto.lastName());
        }
        if (telegramAuthDto.id() != null) {
            data.put("id", telegramAuthDto.id().toString());
        }
        if (telegramAuthDto.username() != null) {
            data.put("username", telegramAuthDto.username());
        }
        if (telegramAuthDto.photoUrl() != null) {
            data.put("photo_url", telegramAuthDto.photoUrl());
        }
        return data;
    }
}
