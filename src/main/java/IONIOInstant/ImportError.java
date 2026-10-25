package IONIOInstant;

import java.util.List;

public record ImportError(int lineNumber, String rawLine, String reason, List<String> errors) {
    public ImportError{

        if (lineNumber == 0 ){
            errors.add("Некорректная строка: " + lineNumber);
        }
        if(rawLine == null){
            errors.add("Некорректная строка: " + rawLine);
        }
        if (reason == null ){
            errors.add("Некорректная строка: " + reason);
        }
    }
}
