package users

import (
	"context"
	"github.com/warmhouse/warmhouse_user_api/internal/entities"

	"github.com/google/uuid"
)

type UsersRepository interface {
	CreateUser(ctx context.Context, user entities.User) error
	GetUser(ctx context.Context, userID uuid.UUID) (entities.User, error)
	UpdateUser(ctx context.Context, user entities.User) error
	GetUserByEmail(ctx context.Context, email string) (entities.User, error)
}
