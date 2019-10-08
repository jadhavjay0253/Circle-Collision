import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.Timer;

class Circle {
    //坐标
    int x;
    int y;
    //速度
    double vx;
    double vy;
    //直径
    int d;

    //球心坐标
    private double xd;
    private double yd;
    //是否发生碰撞，0表示没有发生碰撞，大于0表示有发生碰撞
    int collisionStatus = 0;

    Color color;

    public Circle(int x, int y, double vx, double vy, int d, Color color) {
        this.x = x;
        this.y = y;
        this.vx = vx;
        this.vy = vy;
        this.d = d;
        xd = x;
        yd = y;
        this.color = color;
    }

    public void changePosition() {
        xd += vx;
        yd += vy;
        x = (int) xd;
        y = (int) yd;
    }

    public void changePosition(int panelWidth, int panelHeight) {
        changePosition();
        if ((x + d) >= panelWidth && vx > 0) {
            vx = -vx;
        }
        if (x <= 0 && vx < 0) {
            vx = -vx;
        }
        if ((y + d) >= panelHeight && vy > 0) {
            vy = -vy;
        }
        if (y <= 0 && vy < 0) {
            vy = -vy;
        }
    }

    private double getDistanceWith(Circle circle) {
        return (x - circle.x) * (x - circle.x) + (y - circle.y) * (y - circle.y);
    }

    public boolean isCollisionWith(Circle circle) {
        double distanceSqr = getDistanceWith(circle);
        if (d * d > distanceSqr) {
            if (collisionStatus == 0) {
                return true;
            } else {
                collisionStatus++;
                circle.collisionStatus++;
                return false;
            }
        } else {
            if (collisionStatus > 0)
                collisionStatus--;
            if (circle.collisionStatus > 0)
                circle.collisionStatus--;
            return false;
        }

    }

    public static void changeSpeedByCollision(Circle circle1, Circle circle2) {
        // 以二元数组代向量
        double x1 = circle1.x + circle1.d / 2.0;
        double x2 = circle2.x + circle2.d / 2.0;
        double y1 = circle1.y + circle1.d / 2.0;
        double y2 = circle2.y + circle2.d / 2.0;

        double[] s = {x2 - x1, y2 - y1};// 两球心连心线方向向量
        double[] t = {y1 - y2, x2 - x1};// 两球切线方向向量

        // 将s,t标准化
        double L = Math.sqrt(s[0] * s[0] + s[1] * s[1]);
        s[0] = s[0] / L;
        s[1] = s[1] / L;
        L = Math.sqrt(t[0] * t[0] + t[1] * t[1]);
        t[0] = t[0] / L;
        t[1] = t[1] / L;

        // 两球碰撞前速度向量
        double[] v1 = {circle1.vx, circle1.vy};
        double[] v2 = {circle2.vx, circle2.vy};
        // 用求数量积的方式求两球速度在s,t方向的投影
        double v1s = v1[0] * s[0] + v1[1] * s[1];
        double v1t = v1[0] * t[0] + v1[1] * t[1];
        double v2s = v2[0] * s[0] + v2[1] * s[1];
        double v2t = v2[0] * t[0] + v2[1] * t[1];
        // 等质量球非对心弹性碰撞，s方向速度交换，t方向速度不变
        double temp_v1s = v2s;
        v2s = v1s;
        v1s = temp_v1s;

        // 得到碰撞后的合速度向量
        double[] v1_new = {t[0] * v1t + s[0] * v1s, t[1] * v1t + s[1] * v1s};
        double[] v2_new = {t[0] * v2t + s[0] * v2s, t[1] * v2t + s[1] * v2s};

        // 划分为x,y方向的分速度
        circle1.vx = v1_new[0];
        circle1.vy = v1_new[1];
        circle2.vx = v2_new[0];
        circle2.vy = v2_new[1];
    }
}

public class Collision extends JFrame {
    private DrawPanel panel;
    private List<Circle> circles = new ArrayList<>();

    public Collision() {
        Circle[] allCircle = new Circle[]{new Circle(100, 100, 4, 6, 200, Color.LIGHT_GRAY),
                new Circle(400, 200, -6, 4, 200, Color.GRAY),
                new Circle(600, 400, -6, -4, 200, Color.DARK_GRAY),
                new Circle(800, 600, 4, -6, 200, Color.WHITE)};
        Collections.addAll(circles, allCircle);

        setTitle("多个球碰撞动画");
        setBounds(100, 100, 1280, 720);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        panel = new DrawPanel();
        add(panel);

        Timer timer = new Timer(16, e -> {
            for (Circle circle : circles) {
                circle.changePosition(panel.getWidth(), panel.getHeight());

            }
            for (int i = 0; i < circles.size(); i++) {
                for (int j = i + 1; j < circles.size(); j++) {
                    if (circles.get(i).isCollisionWith(circles.get(j))) {
                        Circle.changeSpeedByCollision(circles.get(i), circles.get(j));
                    }
                }
            }
            panel.repaint();
        });
        timer.start();
    }

    private class DrawPanel extends JPanel {
        @Override
        public void paint(Graphics g) {
            super.paint(g);
            Graphics2D g2 = (Graphics2D) g;
            g2.setStroke(new BasicStroke(1, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            for (Circle circle : circles) {
                Shape c1 = new Ellipse2D.Double(circle.x, circle.y, circle.d, circle.d);
                g2.setColor(circle.color);
                g2.fill(c1);
                g2.setColor(Color.BLACK);
                g2.draw(c1);
            }
        }

    }

    public static void main(String[] args) {
        new Collision().setVisible(true);
    }
}
