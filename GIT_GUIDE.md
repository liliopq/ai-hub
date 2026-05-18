# Git 使用指南

## 📋 项目已启用 Git 版本控制

### ✅ 安全保护

以下敏感文件已被 `.gitignore` 忽略，**不会**被提交到 Git：
- `backend/.env` - 包含数据库密码、API Key 等敏感信息
- `frontend/.env` - 前端环境变量
- `node_modules/` - 依赖包
- `target/` - 编译产物
- `dist/` - 构建产物
- IDE 配置文件（`.idea/`, `.vscode/`）

### 🚀 常用 Git 命令

#### 1. 查看状态
```bash
git status
```

#### 2. 查看修改
```bash
git diff
```

#### 3. 添加文件到暂存区
```bash
# 添加所有修改
git add .

# 添加指定文件
git add src/main/java/com/ai_hub/controller/PostController.java
```

#### 4. 提交修改
```bash
git commit -m "描述你的修改内容"
```

#### 5. 查看历史
```bash
# 简洁视图
git log --oneline

# 详细视图
git log

# 图形化视图
git log --graph --oneline --all
```

#### 6. 撤销操作
```bash
# 撤销工作区的修改（未 add）
git checkout -- <file>

# 撤销暂存区的文件（已 add，未 commit）
git reset HEAD <file>

# 撤销最后一次提交（保留修改）
git reset --soft HEAD~1
```

### 🌿 分支管理

#### 创建新分支
```bash
git branch feature/new-feature
git checkout feature/new-feature
# 或简写为
git checkout -b feature/new-feature
```

#### 切换分支
```bash
git checkout main
```

#### 合并分支
```bash
git checkout main
git merge feature/new-feature
```

#### 删除分支
```bash
git branch -d feature/new-feature
```

### 🔄 工作流程建议

#### 功能开发流程
```bash
# 1. 从 main 创建新功能分支
git checkout -b feature/user-authentication

# 2. 开发功能并提交
git add .
git commit -m "Add user authentication feature"

# 3. 测试完成后合并到 main
git checkout main
git merge feature/user-authentication
```

#### Bug 修复流程
```bash
# 1. 创建修复分支
git checkout -b fix/login-error

# 2. 修复 bug 并提交
git add .
git commit -m "Fix login error when password is empty"

# 3. 合并到 main
git checkout main
git merge fix/login-error
```

### ⚠️ 重要提醒

1. **永远不要提交敏感信息**
   - 数据库密码
   - API Keys
   - AccessKey Secret
   - JWT Secret
   
2. **提交前检查**
   ```bash
   git status
   git diff
   ```

3. **编写清晰的提交信息**
   ```bash
   # ✅ 好的示例
   git commit -m "Add Redis caching for post list"
   git commit -m "Fix: Handle null pointer in comment service"
   
   # ❌ 不好的示例
   git commit -m "update"
   git commit -m "fix bug"
   ```

4. **定期提交**
   - 完成一个小功能就提交
   - 不要积累太多修改再提交

### 🔐 如果不小心提交了敏感信息

```bash
# 1. 立即从 Git 历史中删除
git filter-branch --force --index-filter \
  'git rm --cached --ignore-unmatch backend/.env' \
  --prune-empty --tag-name-filter cat -- --all

# 2. 添加到 .gitignore
echo "backend/.env" >> .gitignore

# 3. 强制推送（如果已经推送到远程）
git push origin --force --all
```

### 📊 当前分支状态

```bash
# 查看所有分支
git branch -a

# 查看当前分支
git branch

# 查看远程仓库
git remote -v
```

---

**祝开发愉快！** 🎉
