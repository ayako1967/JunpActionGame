package jp.techacademy.takuya.okitsu.jumpactiongame;

/**
 * Created by takuy on 2016/11/19.
 */
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.Vector2;

public class GameObject extends Sprite{
    public final Vector2 velocity;//x.y方向の速度を保持する

    public GameObject(Texture texture,int srcX,int srcY,int srcWidth,int srcHeight) {
        super(texture,srcX,srcY,srcWidth,srcHeight);

        velocity = new Vector2();
    }
}
