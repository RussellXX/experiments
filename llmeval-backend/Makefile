ROOT_DIR := $(shell pwd)
PYTHON_WORKDIR := $(ROOT_DIR)/llmeval-python
LOG_DIR := $(ROOT_DIR)/logs
PID_DIR := $(ROOT_DIR)/pids

.PHONY: python-build python-run python-clean java-build java-run java-clean clean-logs build run test

# Python 环境构建
python-build:
	@echo "Building Python environment..."
	@if [ -f "$(PYTHON_WORKDIR)/.dependencies_installed" ]; then \
		echo "[INFO] Dependencies already installed. Skipping installation."; \
	else \
		cd $(PYTHON_WORKDIR) && pip install -r requirements.txt --target=./ --no-deps --no-index 2>/dev/null || pip install -r requirements.txt --target=./; \
		touch $(PYTHON_WORKDIR)/.dependencies_installed; \
	fi

# Python 运行
python-run:
	@echo "[INFO] Ensuring cgroup directory exists..."
	@sudo mkdir -p /sys/fs/cgroup/my_cgroup

	@echo "[INFO] Setting memory limit..."
	@echo "10G" | sudo tee /sys/fs/cgroup/my_cgroup/memory.max > /dev/null

	@echo "[INFO] Creating log directory..."
	@mkdir -p $(LOG_DIR)

	@echo "[INFO] Running Python script inside cgroup..."
		@cd $(PYTHON_WORKDIR) && \
		sudo cgexec -g memory,cpu:/my_cgroup \
		sh -c 'echo $$PPID > $(PID_DIR)/python.pid && exec python main.py' >> $(LOG_DIR)/python-run.log 2>&1 &
	@echo "[INFO] Python service started with PID $$(cat $(PID_DIR)/python.pid)"

# 清理 Python 环境
python-clean:
	@echo "Cleaning Python environment..."
	@find $(PYTHON_WORKDIR) -mindepth 1 -depth \
		! -path "$(PYTHON_WORKDIR)/llmeval" \
		! -path "$(PYTHON_WORKDIR)/llmeval/*" \
		! -path "$(PYTHON_WORKDIR)/main.py" \
		! -path "$(PYTHON_WORKDIR)/requirements.txt" \
		-exec rm -rf {} + 2>/dev/null
	@rm -f $(PYTHON_WORKDIR)/.dependencies_installed

# Java 构建
java-build:
	@echo "[INFO] Building Java project..."
	@mvn package -DskipTests

# Java 运行 (在 cgroup 中)
java-run:
	@echo "[INFO] Ensuring cgroup directory exists..."
	@sudo mkdir -p /sys/fs/cgroup/my_cgroup

	@echo "[INFO] Setting memory limit..."
	@echo "10G" | sudo tee /sys/fs/cgroup/my_cgroup/memory.max > /dev/null

	@mkdir -p $(LOG_DIR)
	@echo "[INFO] Running Java project inside cgroup..."
	@sudo cgexec -g memory,cpu:/my_cgroup java -jar target/LLMEval-0.0.1-SNAPSHOT.jar >> $(LOG_DIR)/java-run.log 2>&1 & \
		echo $$! > $(PID_DIR)/java.pid
	@echo "[INFO] Java service started with PID $$(cat $(PID_DIR)/java.pid)"
	@echo "[INFO] Logs are saved in $(LOG_DIR)/java-run.log"

# 清理 Java 项目
java-clean:
	@echo "Cleaning Java environment..."
	@rm -rf $(LOG_DIR) target

# 删除日志
clean-logs:
	@echo "[INFO] Cleaning logs..."
	@rm -rf $(LOG_DIR)

# 构建所有组件
build: python-build java-build
	@echo "All components built successfully."

# 运行所有组件
run:
	@echo "[INFO] Starting services..."
	@mkdir -p $(PID_DIR)
	@$(MAKE) python-run
	@$(MAKE) java-run
	@echo "[INFO] All services started. Press Ctrl+C to stop."
	@echo "[INFO] Python PID: $$(cat $(PID_DIR)/python.pid), Java PID: $$(cat $(PID_DIR)/java.pid)"
	@sleep 2  # Give services time to start
	@echo "[INFO] Setting up signal handler..."
	@trap 'make kill-all' INT; while [ -f $(PID_DIR)/java.pid ] && [ -f $(PID_DIR)/python.pid ]; do sleep 1; done

# Kill all services
kill-all:
	@echo "[INFO] Stopping all services..."
	@if [ -f $(PID_DIR)/python.pid ]; then \
		echo "[INFO] Killing Python (PID: $$(cat $(PID_DIR)/python.pid))"; \
		sudo kill -15 $$(cat $(PID_DIR)/python.pid) 2>/dev/null || true; \
		rm -f $(PID_DIR)/python.pid; \
	fi
	@if [ -f $(PID_DIR)/java.pid ]; then \
		echo "[INFO] Killing Java (PID: $$(cat $(PID_DIR)/java.pid))"; \
		sudo kill -15 $$(cat $(PID_DIR)/java.pid) 2>/dev/null || true; \
		rm -f $(PID_DIR)/java.pid; \
	fi
	@rm -rf $(PID_DIR)
	@echo "[INFO] All services stopped."
	@$(MAKE) kill-stray
	
# 测试 Python 和 Java
test:
	@echo "[INFO] Running tests..."
	@mkdir -p $(PID_DIR)
	@$(MAKE) python-run
	@mvn test
	@$(MAKE) kill-all

# 杀死所有可能存留的相关进程
kill-stray:
	@echo "[INFO] Killing any stray processes..."
	@ps aux | grep 'python.*llmeval-python/main.py' | grep -v 'grep' | awk '{print $$2}' | xargs -r sudo kill -9 2>/dev/null || true
	@ps aux | grep 'python main.py' | grep -v 'grep' | awk '{print $$2}' | xargs -r sudo kill -9 2>/dev/null || true
	@ps aux | grep 'cgexec.*python' | grep -v 'grep' | awk '{print $$2}' | xargs -r sudo kill -9 2>/dev/null || true
