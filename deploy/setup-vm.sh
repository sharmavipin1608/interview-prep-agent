#!/bin/bash
set -e

echo "==> Updating system..."
sudo apt update && sudo apt upgrade -y

echo "==> Installing Docker..."
curl -fsSL https://get.docker.com | sudo sh
sudo usermod -aG docker ubuntu

echo "==> Installing Caddy..."
sudo apt install -y debian-keyring debian-archive-keyring apt-transport-https
curl -1sLf 'https://dl.cloudsmith.io/public/caddy/stable/gpg.key' \
  | sudo gpg --dearmor -o /usr/share/keyrings/caddy-stable-archive-keyring.gpg
curl -1sLf 'https://dl.cloudsmith.io/public/caddy/stable/debian.deb.txt' \
  | sudo tee /etc/apt/sources.list.d/caddy-stable.list
sudo apt update && sudo apt install -y caddy

echo "==> Installing iptables-persistent..."
sudo apt install -y iptables-persistent

echo "==> Opening ports 80 and 443..."
sudo iptables -I INPUT -p tcp --dport 80 -j ACCEPT
sudo iptables -I INPUT -p tcp --dport 443 -j ACCEPT
sudo netfilter-persistent save

echo "==> Creating app directory..."
sudo mkdir -p /opt/interview-prep-agent
sudo chown ubuntu:ubuntu /opt/interview-prep-agent

echo ""
echo "==> Setup complete!"
echo ""
echo "Next: create /opt/interview-prep-agent/.env with your secrets:"
echo ""
echo "  nano /opt/interview-prep-agent/.env"
echo ""
echo "Paste in:"
echo "  OPENAI_API_KEY=sk-..."
echo "  TAVILY_API_KEY=tvly-..."
echo "  POSTGRES_PASSWORD=choose-a-strong-password"
echo ""
echo "Then log out and back in for Docker group membership to take effect."
