import { Button, Input, Picker, Text, View } from '@tarojs/components';
import Taro, { useDidShow, useRouter } from '@tarojs/taro';
import { useState } from 'react';
import { createTodo, getTodos, toggleTodo, type SpaceTodo } from '../../api/todo';
import { EmptyState } from '../../components/EmptyState';
import { PageShell } from '../../components/PageShell';
import { useAppStore } from '../../store/appStore';
import { requireLogin } from '../../utils/auth';
import { formatDateTime } from '../../utils/format';
import './index.scss';

export default function TodosPage() {
  const router = useRouter();
  const relationships = useAppStore((state) => state.relationships);
  const refreshRelationshipsAndTheme = useAppStore((state) => state.refreshRelationshipsAndTheme);
  const [relationshipIndex, setRelationshipIndex] = useState(0);
  const [todos, setTodos] = useState<SpaceTodo[]>([]);
  const [title, setTitle] = useState('');
  const [loading, setLoading] = useState(false);

  async function prepareRelationships() {
    const rels = relationships.length ? relationships : await refreshRelationshipsAndTheme();
    const queryId = router.params.relationshipId;
    if (queryId) {
      const foundIndex = rels.findIndex((item) => String(item.id) === String(queryId));
      if (foundIndex >= 0) setRelationshipIndex(foundIndex);
    }
    return rels;
  }

  async function loadTodos() {
    if (!requireLogin()) return;
    setLoading(true);
    try {
      const rels = await prepareRelationships();
      const relationship = rels[relationshipIndex] || rels[0];
      if (!relationship) {
        setTodos([]);
        return;
      }
      const data = await getTodos(relationship.id, { page: 1, size: 30 });
      setTodos(data);
    } finally {
      setLoading(false);
    }
  }

  async function submitTodo() {
    const relationship = relationships[relationshipIndex];
    if (!relationship || !title.trim()) return;
    await createTodo(relationship.id, { title: title.trim(), priority: 'NORMAL' });
    setTitle('');
    Taro.showToast({ title: '创建成功', icon: 'success' });
    loadTodos();
  }

  async function handleToggle(todo: SpaceTodo) {
    const relationship = relationships[relationshipIndex];
    if (!relationship) return;
    await toggleTodo(relationship.id, todo.id);
    loadTodos();
  }

  useDidShow(() => {
    loadTodos();
  });

  return (
    <PageShell>
      <View className="page todos-page">
        <View className="todos-filter card">
          <Picker
            mode="selector"
            range={relationships.map((item) => item.name)}
            value={relationshipIndex}
            onChange={(event) => {
              setRelationshipIndex(Number(event.detail.value));
              setTimeout(loadTodos, 0);
            }}
          >
            <View className="todos-picker">{relationships[relationshipIndex]?.name || '请选择关系空间'}</View>
          </Picker>
          <View className="todos-create">
            <Input className="todos-input" value={title} placeholder="新增代办" onInput={(event) => setTitle(event.detail.value)} />
            <Button className="primary-button todos-create__button" onClick={submitTodo}>
              添加
            </Button>
          </View>
        </View>

        {todos.length ? (
          todos.map((item) => (
            <View className={`todo-card card ${item.status === 'DONE' ? 'todo-card--done' : ''}`} key={item.id}>
              <View>
                <Text className="todo-card__title">{item.title}</Text>
                <Text className="todo-card__meta">{item.priority || 'NORMAL'} · {formatDateTime(item.dueTime)}</Text>
              </View>
              <Button className="ghost-button todo-card__button" onClick={() => handleToggle(item)}>
                {item.status === 'DONE' ? '重开' : '完成'}
              </Button>
            </View>
          ))
        ) : (
          <EmptyState title={loading ? '加载中...' : '今天没有待办'} />
        )}
      </View>
    </PageShell>
  );
}
