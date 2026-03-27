package com.swmschmidt.td.infrastructure.rendering.swing;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.atomic.AtomicBoolean;

public final class SwingGameWindow {
    private final JFrame frame;
    private final RenderPanel renderPanel;
    private final AtomicBoolean open;

    public SwingGameWindow(String title, int width, int height) {
        this.open = new AtomicBoolean(true);
        this.renderPanel = new RenderPanel();
        this.frame = createFrame(title, width, height, renderPanel, open);
    }

    public void showWindow() {
        SwingUtilities.invokeLater(() -> frame.setVisible(true));
    }

    public void present(BufferedImage image) {
        renderPanel.setFrame(image);
    }

    public JFrame frame() {
        return frame;
    }

    public JPanel renderSurface() {
        return renderPanel;
    }

    public boolean isOpen() {
        return open.get();
    }

    public void close() {
        open.set(false);
        SwingUtilities.invokeLater(frame::dispose);
    }

    private static JFrame createFrame(
        String title,
        int width,
        int height,
        RenderPanel panel,
        AtomicBoolean openFlag
    ) {
        final JFrame[] holder = new JFrame[1];
        Runnable initializer = () -> {
            JFrame frame = new JFrame(title);
            frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            panel.setPreferredSize(new Dimension(width, height));
            frame.setMinimumSize(new Dimension(640, 360));
            frame.setContentPane(panel);
            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.addWindowListener(new java.awt.event.WindowAdapter() {
                @Override
                public void windowClosing(java.awt.event.WindowEvent e) {
                    openFlag.set(false);
                }

                @Override
                public void windowClosed(java.awt.event.WindowEvent e) {
                    openFlag.set(false);
                }
            });
            holder[0] = frame;
        };

        if (SwingUtilities.isEventDispatchThread()) {
            initializer.run();
        } else {
            try {
                SwingUtilities.invokeAndWait(initializer);
            } catch (InterruptedException exception) {
                Thread.currentThread().interrupt();
                throw new IllegalStateException("Interrupted while creating game window", exception);
            } catch (InvocationTargetException exception) {
                throw new IllegalStateException("Failed to create game window", exception);
            }
        }
        return holder[0];
    }

    private static final class RenderPanel extends JPanel {
        private volatile BufferedImage frame;

        private RenderPanel() {
            setDoubleBuffered(true);
            setFocusable(true);
            requestFocusInWindow();
        }

        private void setFrame(BufferedImage frame) {
            this.frame = frame;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            super.paintComponent(graphics);
            BufferedImage image = frame;
            if (image != null) {
                graphics.drawImage(image, 0, 0, getWidth(), getHeight(), null);
            }
        }
    }
}
