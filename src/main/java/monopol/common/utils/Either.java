package monopol.common.utils;

import java.util.Optional;

public class Either<L, R> {
    private L left;
    private R right;

    public Either() {}

    public Either(L left, R right) {
        this.left = left;
        this.right = right;
        if((left != null && right != null) || (left == null && right == null)) throw new IllegalStateException();
    }

    public Optional<L> getLeft() {
        return Optional.ofNullable(left);
    }

    public void setLeft(L left) {
        this.left = left;
        right = null;
    }

    public Optional<R> getRight() {
        return Optional.ofNullable(right);
    }

    public void setRight(R right) {
        this.right = right;
        left = null;
    }
}
