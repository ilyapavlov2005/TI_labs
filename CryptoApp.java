import java.util.List;
import java.util.Scanner;//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.nio.file.*;
import java.util.*;

public class CryptoApp extends JFrame {

    private JTextArea inputArea, outputArea;
    private JTextField keyField1, keyField2;
    private JComboBox<String> methodBox, langBox;
    private JButton encryptBtn, decryptBtn, loadBtn, saveBtn;

    private static final String RUS = "абвгдеёжзийклмнопрстуфхцчшщъыьэюя";
    private static final String ENG = "abcdefghijklmnopqrstuvwxyz";
    private String alphabet = RUS;

    public CryptoApp() {
        setTitle("Шифратор / Дешифратор");
        setSize(750, 520);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // Верхняя панель
        JPanel topPanel = new JPanel(new GridLayout(4, 2, 5, 5));
        topPanel.add(new JLabel("Метод:"));
        methodBox = new JComboBox<>(new String[]{
                "Виженер (самогенерирующийся ключ)",
                "Двойной столбцовый шифр"
        });
        topPanel.add(methodBox);

        topPanel.add(new JLabel("Язык:"));
        langBox = new JComboBox<>(new String[]{"Русский", "Английский"});
        topPanel.add(langBox);

        topPanel.add(new JLabel("Ключ 1:"));
        keyField1 = new JTextField();
        topPanel.add(keyField1);

        topPanel.add(new JLabel("Ключ 2:"));
        keyField2 = new JTextField();
        topPanel.add(keyField2);

        add(topPanel, BorderLayout.NORTH);

        // Текстовые поля
        inputArea = new JTextArea();
        outputArea = new JTextArea();
        outputArea.setEditable(false);

        JPanel textPanel = new JPanel(new GridLayout(1, 2));
        textPanel.add(new JScrollPane(inputArea));
        textPanel.add(new JScrollPane(outputArea));
        add(textPanel, BorderLayout.CENTER);

        // Кнопки
        JPanel bottomPanel = new JPanel();
        loadBtn = new JButton("Загрузить");
        saveBtn = new JButton("Сохранить");
        encryptBtn = new JButton("Зашифровать");
        decryptBtn = new JButton("Расшифровать");

        bottomPanel.add(loadBtn);
        bottomPanel.add(saveBtn);
        bottomPanel.add(encryptBtn);
        bottomPanel.add(decryptBtn);
        add(bottomPanel, BorderLayout.SOUTH);

        // Обработчики
        encryptBtn.addActionListener(e -> process(true));
        decryptBtn.addActionListener(e -> process(false));
        loadBtn.addActionListener(e -> loadFile());
        saveBtn.addActionListener(e -> saveFile());
        langBox.addActionListener(e -> setLangAlphabet());

        // Показываем/скрываем второй ключ
        methodBox.addActionListener(e -> updateKeyFieldsVisibility());
        updateKeyFieldsVisibility();
    }

    private void updateKeyFieldsVisibility() {
        boolean isColumnar = methodBox.getSelectedIndex() == 1;
        Component label2 = ((JPanel)getContentPane().getComponent(0)).getComponent(6);
        if (label2 != null) {
            label2.setVisible(isColumnar);
        }
        keyField2.setVisible(isColumnar);
    }

    private void setLangAlphabet() {
        alphabet = (langBox.getSelectedIndex() == 0) ? RUS : ENG;
    }

    private String cleanKey(String key) {
        // Для ключа убираем всё кроме букв алфавита
        StringBuilder result = new StringBuilder();
        String upperAlphabet = alphabet.toUpperCase();
        key = key.toUpperCase();

        for (char c : key.toCharArray()) {
            if (upperAlphabet.indexOf(c) >= 0) {
                result.append(c);
            }
        }
        return result.toString();
    }

    private void process(boolean encrypt) {
        setLangAlphabet();

        String input = inputArea.getText();
        String key1 = cleanKey(keyField1.getText());
        String key2 = cleanKey(keyField2.getText());

        if (input.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Введите текст для обработки!");
            return;
        }

        if (methodBox.getSelectedIndex() == 0) {
            // Виженер
            if (key1.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Введите ключ!");
                return;
            }

            String result;
            if (encrypt) {
                result = vigenereAutoEncrypt(input, key1);
            } else {
                result = vigenereAutoDecrypt(input, key1);
            }
            outputArea.setText(result);

        } else {
            // Двойной столбцовый шифр
            if (key1.isEmpty() || key2.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Введите оба ключа!");
                return;
            }

            String result;
            if (encrypt) {
                result = doubleColumnarEncrypt(input, key1, key2);
                outputArea.setText(result);
            } else {
                result = doubleColumnarDecrypt(input, key1, key2);
                outputArea.setText(result);
            }
        }
    }

    // === ВИЖЕНЕР ===

    private String vigenereAutoEncrypt(String text, String key) {
        String upperAlphabet = alphabet.toUpperCase();
        StringBuilder result = new StringBuilder();
        StringBuilder dynamicKey = new StringBuilder(key);

        int keyIndex = 0;

        for (int i = 0; i < text.length(); i++) {
            char textChar = text.charAt(i);

            if (upperAlphabet.indexOf(Character.toUpperCase(textChar)) >= 0) {
                char keyChar = dynamicKey.charAt(keyIndex);

                int textIdx = upperAlphabet.indexOf(Character.toUpperCase(textChar));
                int keyIdx = upperAlphabet.indexOf(keyChar);

                int encryptedIdx = (textIdx + keyIdx) % upperAlphabet.length();
                char encryptedChar = upperAlphabet.charAt(encryptedIdx);

                if (Character.isUpperCase(textChar)) {
                    result.append(encryptedChar);
                } else {
                    result.append(Character.toLowerCase(encryptedChar));
                }

                dynamicKey.append(Character.toUpperCase(textChar));
                keyIndex++;
            } else {
                result.append(textChar);
            }
        }

        return result.toString();
    }

    private String vigenereAutoDecrypt(String text, String key) {
        String upperAlphabet = alphabet.toUpperCase();
        StringBuilder result = new StringBuilder();
        StringBuilder dynamicKey = new StringBuilder(key);

        int keyIndex = 0;

        for (int i = 0; i < text.length(); i++) {
            char cipherChar = text.charAt(i);

            if (upperAlphabet.indexOf(Character.toUpperCase(cipherChar)) >= 0) {
                char keyChar = dynamicKey.charAt(keyIndex);

                int cipherIdx = upperAlphabet.indexOf(Character.toUpperCase(cipherChar));
                int keyIdx = upperAlphabet.indexOf(keyChar);

                int decryptedIdx = (cipherIdx - keyIdx + upperAlphabet.length()) % upperAlphabet.length();
                char decryptedChar = upperAlphabet.charAt(decryptedIdx);

                if (Character.isUpperCase(cipherChar)) {
                    result.append(decryptedChar);
                } else {
                    result.append(Character.toLowerCase(decryptedChar));
                }

                dynamicKey.append(decryptedChar);
                keyIndex++;
            } else {
                result.append(cipherChar);
            }
        }

        return result.toString();
    }

    // === ДВОЙНОЙ СТОЛБЦОВЫЙ ШИФР (игнорирует всё кроме букв и пробелов) ===

    private String doubleColumnarEncrypt(String text, String key1, String key2) {
        // Сначала обрабатываем текст: оставляем только буквы и пробелы
        String processedText = keepLettersAndSpaces(text);

        // Шифруем первым ключом
        String temp = columnarEncryptInternal(processedText, key1);
        // Шифруем вторым ключом
        return columnarEncryptInternal(temp, key2);
    }

    private String doubleColumnarDecrypt(String text, String key1, String key2) {
        // Дешифруем вторым ключом (обратный порядок)
        String temp = columnarDecryptInternal(text, key2);
        // Дешифруем первым ключом
        String result = columnarDecryptInternal(temp, key1);

        // Восстанавливаем исходный текст с учетом регистра
        return restoreOriginalText(result, inputArea.getText());
    }

    private String keepLettersAndSpaces(String text) {
        // Оставляем только буквы выбранного языка и пробелы
        StringBuilder result = new StringBuilder();
        String upperAlphabet = alphabet.toUpperCase();
        String lowerAlphabet = alphabet.toLowerCase();

        for (char c : text.toCharArray()) {
            if (c == ' ') {
                result.append(' ');
            } else if (upperAlphabet.indexOf(c) >= 0 || lowerAlphabet.indexOf(c) >= 0) {
                result.append(Character.toUpperCase(c));
            }
            // Все остальные символы (цифры, знаки препинания) игнорируем
        }

        return result.toString();
    }

    private String columnarEncryptInternal(String text, String key) {
        String upperAlphabet = alphabet.toUpperCase();
        String keyStr = key.toUpperCase();

        // Убираем пробелы для шифрования, но запоминаем их позиции
        StringBuilder lettersOnly = new StringBuilder();
        List<Integer> spacePositions = new ArrayList<>();

        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (c == ' ') {
                spacePositions.add(i);
            } else {
                lettersOnly.append(c);
            }
        }

        String textStr = lettersOnly.toString();

        if (textStr.isEmpty()) {
            return text; // Если нет букв, возвращаем исходный текст
        }

        int columns = keyStr.length();
        int rows = (int) Math.ceil((double) textStr.length() / columns);

        // Создаем таблицу
        char[][] table = new char[rows][columns];

        // Заполняем по строкам
        int index = 0;
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < columns; c++) {
                if (index < textStr.length()) {
                    table[r][c] = textStr.charAt(index++);
                } else {
                    table[r][c] = (upperAlphabet.equals(ENG.toUpperCase())) ? 'X' : 'Ъ';
                }
            }
        }

        // Получаем порядок столбцов
        int[] order = getColumnOrder(keyStr);

        // Читаем по столбцам
        StringBuilder encryptedLetters = new StringBuilder();
        for (int col : order) {
            for (int r = 0; r < rows; r++) {
                encryptedLetters.append(table[r][col]);
            }
        }

        // Вставляем пробелы обратно
        StringBuilder result = new StringBuilder(encryptedLetters.toString());
        for (int pos : spacePositions) {
            if (pos < result.length()) {
                result.insert(pos, ' ');
            }
        }

        return result.toString();
    }

    private String columnarDecryptInternal(String text, String key) {
        String upperAlphabet = alphabet.toUpperCase();
        String keyStr = key.toUpperCase();

        // Убираем пробелы для дешифрования, но запоминаем их позиции
        StringBuilder lettersOnly = new StringBuilder();
        List<Integer> spacePositions = new ArrayList<>();

        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (c == ' ') {
                spacePositions.add(i);
            } else {
                lettersOnly.append(c);
            }
        }

        String textStr = lettersOnly.toString();

        if (textStr.isEmpty()) {
            return text;
        }

        int columns = keyStr.length();

        // Проверяем, что длина текста кратна количеству столбцов
        if (textStr.length() % columns != 0) {
            int neededLength = ((textStr.length() / columns) + 1) * columns;
            while (textStr.length() < neededLength) {
                textStr += (upperAlphabet.equals(ENG.toUpperCase())) ? 'X' : 'Ъ';
            }
        }

        int rows = textStr.length() / columns;

        // Получаем порядок столбцов
        int[] encryptOrder = getColumnOrder(keyStr);

        // Создаем обратный порядок
        int[] decryptOrder = new int[columns];
        for (int i = 0; i < columns; i++) {
            decryptOrder[encryptOrder[i]] = i;
        }

        // Создаем таблицу
        char[][] table = new char[rows][columns];

        // Заполняем по столбцам
        int index = 0;
        for (int c = 0; c < columns; c++) {
            for (int r = 0; r < rows; r++) {
                table[r][encryptOrder[c]] = textStr.charAt(index++);
            }
        }

        // Читаем по строкам
        StringBuilder decryptedLetters = new StringBuilder();
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < columns; c++) {
                decryptedLetters.append(table[r][c]);
            }
        }

        // Вставляем пробелы обратно
        StringBuilder result = new StringBuilder(decryptedLetters.toString());
        for (int pos : spacePositions) {
            if (pos < result.length()) {
                result.insert(pos, ' ');
            }
        }

        return result.toString();
    }

    private String restoreOriginalText(String decrypted, String original) {
        // Восстанавливаем регистр и игнорируемые символы
        StringBuilder result = new StringBuilder();
        String upperAlphabet = alphabet.toUpperCase();
        String lowerAlphabet = alphabet.toLowerCase();

        int decryptedIndex = 0;

        for (int i = 0; i < original.length(); i++) {
            char originalChar = original.charAt(i);

            if (originalChar == ' ') {
                result.append(' ');
            } else if (upperAlphabet.indexOf(originalChar) >= 0 ||
                    lowerAlphabet.indexOf(originalChar) >= 0) {
                // Это буква - берем из расшифрованного текста
                if (decryptedIndex < decrypted.length()) {
                    char decryptedChar = decrypted.charAt(decryptedIndex++);
                    // Восстанавливаем регистр
                    if (Character.isUpperCase(originalChar)) {
                        result.append(Character.toUpperCase(decryptedChar));
                    } else {
                        result.append(Character.toLowerCase(decryptedChar));
                    }
                }
            } else {
                // Игнорируемые символы (цифры, знаки) - пропускаем
                // Не добавляем их в результат
            }
        }

        return result.toString();
    }

    private int[] getColumnOrder(String key) {
        int n = key.length();

        List<Map.Entry<Character, Integer>> list = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            list.add(new AbstractMap.SimpleEntry<>(key.charAt(i), i));
        }

        list.sort(Map.Entry.comparingByKey());

        int[] order = new int[n];
        for (int i = 0; i < n; i++) {
            order[i] = list.get(i).getValue();
        }

        return order;
    }

    private void loadFile() {
        JFileChooser fc = new JFileChooser();
        if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            try {
                String content = Files.readString(fc.getSelectedFile().toPath(), java.nio.charset.StandardCharsets.UTF_8);
                inputArea.setText(content);
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, "Ошибка чтения файла!");
            }
        }
    }

    private void saveFile() {
        JFileChooser fc = new JFileChooser();
        if (fc.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            try {
                Files.writeString(fc.getSelectedFile().toPath(), outputArea.getText(), java.nio.charset.StandardCharsets.UTF_8);
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, "Ошибка записи файла!");
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new CryptoApp().setVisible(true));
    }
}