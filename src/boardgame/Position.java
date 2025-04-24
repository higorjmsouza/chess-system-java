package boardgame;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
public class Position {

    private int row;
    private int column;

    public void setValues(final int row, final int column) {
        this.row = row;
        this.column = column;
    }

    @Override
    public String toString() {
        return "%s, %s".formatted(row, column);
    }
}
