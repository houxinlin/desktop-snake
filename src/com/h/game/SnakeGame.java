package com.h.game;

import com.h.game.jni.GameJNI;
import com.h.game.jni.HotkeyCallback;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class SnakeGame extends JFrame implements HotkeyCallback {
    private static final int WINDOW_WIDTH = 300;
    private static final int WINDOW_HEIGHT = 200;
    private GameJNI gameJNI = new GameJNI();
    private ScheduledExecutorService scheduledExecutorService = new ScheduledThreadPoolExecutor(1);
    private Direction direction = Direction.RIGHT;
    private Point snakePoint = new Point(0, 0);
    private static final int MOVE_SIZE = 84;
    private List<Point> snakeBodyPoint = new ArrayList<>();
    private Point foodPoint = new Point(0, 0);
    private List<Point> allPoint = generatorPoints();
    private ScheduledFuture<?> scheduledFuture = null;

    public SnakeGame() {
        this.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        this.setLocation(screenSize.width / 2 - WINDOW_WIDTH / 2, screenSize.height / 2 - WINDOW_HEIGHT / 2);
        this.setSize(WINDOW_WIDTH, WINDOW_HEIGHT);
        init();
        this.setVisible(true);
    }

    private void startGame() {
        this.setVisible(false);
        if (scheduledFuture == null) {
            scheduledFuture = scheduledExecutorService.scheduleAtFixedRate(() -> {
                Point oldPoint = snakePoint.getLocation();
                if (direction == Direction.LEFT) snakePoint.x -= MOVE_SIZE;
                if (direction == Direction.UP) snakePoint.y -= MOVE_SIZE;
                if (direction == Direction.RIGHT) snakePoint.x += MOVE_SIZE;
                if (direction == Direction.DOWN) snakePoint.y += MOVE_SIZE;
                moveSnakeHeader();
                moveSnakeBody(oldPoint);
                isCollision();
            }, 0, 200, TimeUnit.MILLISECONDS);
        }

    }


    private void isCollision() {
        if (snakeBodyPoint.stream().anyMatch(point -> point.equals(snakePoint))) {
            scheduledFuture.cancel(true);
            scheduledFuture = null;
            this.setVisible(true);
        }
    }

    private void moveSnakeHeader() {
        gameJNI.moveDesktopIcon(0, snakePoint.x, snakePoint.y);
        if (foodPoint.equals(snakePoint)) resetBodyLocation();
    }

    private List<Point> generatorPoints() {
        List<Point> all = new ArrayList<>();
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        for (int i = 0; i < screenSize.width; i += MOVE_SIZE) {
            for (int j = 0; j < screenSize.height; j += MOVE_SIZE) {
                all.add(new Point(i, j));
            }
        }
        return all;
    }

    private void resetBodyLocation() {
        List<Point> newPoint = allPoint.stream().filter(point -> !has(point)).collect(Collectors.toList());
        Collections.shuffle(newPoint);
        Point point = newPoint.get(0);
        int desktopIcon = gameJNI.getDesktopIcon();
        foodPoint.setLocation(point.x, point.y);
        gameJNI.moveDesktopIcon(desktopIcon - 1, point.x, point.y);
    }

    private boolean has(Point hasPoint) {
        return snakeBodyPoint.stream().anyMatch(point -> hasPoint.equals(point));
    }

    private void moveSnakeBody(Point oldPoint) {
        for (int i = 1; i < snakeBodyPoint.size() - 1; i++) {
            Point itemPoint = snakeBodyPoint.get(i);
            gameJNI.moveDesktopIcon(i, oldPoint.x, oldPoint.y);
            snakeBodyPoint.set(i, oldPoint.getLocation());
            oldPoint = itemPoint;
        }
    }


    private void init() {
        this.setLayout(new BorderLayout());
        String str ="<html>首先右击桌面，查看>取消自动排列图片、将网格与图片对齐。方向键为← ↑ → ↓</html>";
        JLabel jLabel = new JLabel(str);
        jLabel.setFont(new Font("黑体",0,18));
        add(jLabel, BorderLayout.NORTH);
        add(createButton(), BorderLayout.SOUTH);
        registerHotkey();
        reset();
    }

    private void reset() {
        snakeBodyPoint.clear();
        direction = Direction.RIGHT;
        snakePoint.setLocation(0, 0);
        int desktopIcon = gameJNI.getDesktopIcon();
        int offsetX = -MOVE_SIZE;
        for (int i = 0; i < desktopIcon; i++) {
            snakeBodyPoint.add(new Point(offsetX, 0));
            gameJNI.moveDesktopIcon(i, offsetX, 0);
            offsetX -= MOVE_SIZE;
        }
        resetBodyLocation();
    }

    private JButton createButton() {


        JButton jButton = new JButton("开始");
        jButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                reset();
                startGame();
            }
        });
        return jButton;
    }

    @Override
    public void hotkey(int key) {
        if (key == 1) direction = Direction.LEFT;
        if (key == 2) direction = Direction.UP;
        if (key == 3) direction = Direction.RIGHT;
        if (key == 4) direction = Direction.DOWN;

    }

    public void registerHotkey() {
        new Thread(() -> gameJNI.registerHotkeyCallback(this)).start();
    }

    enum Direction {
        LEFT, UP, RIGHT, DOWN
    }
}
