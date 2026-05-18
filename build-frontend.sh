#!/bin/bash

cd "$(dirname "$0")/yanwai-frontend"

echo "Installing dependencies..."
npm install

echo "Building frontend..."
npm run build

echo "Build completed! Output in dist/"
