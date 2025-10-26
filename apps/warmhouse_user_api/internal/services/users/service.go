package users

import (
	"context"
	"database/sql"
	"errors"
	"fmt"
	"time"

	"github.com/warmhouse/warmhouse_user_api/internal/entities"
	"github.com/warmhouse/warmhouse_user_api/internal/generated/server"
	"github.com/warmhouse/warmhouse_user_api/internal/utils"

	"github.com/warmhouse/libraries/convert"

	"github.com/google/uuid"
	"golang.org/x/crypto/bcrypt"
)

type Service struct {
	usersRepository UsersRepository
	jwtManager      *utils.JWTManager
}

func NewService(usersRepository UsersRepository, jwtManager *utils.JWTManager) *Service {
	return &Service{
		usersRepository: usersRepository,
		jwtManager:      jwtManager,
	}
}

func (s *Service) RegisterUser(ctx context.Context, request server.RegisterUserRequestObject) (uuid.UUID, error) {
	hashedPassword, err := bcrypt.GenerateFromPassword([]byte(request.Body.Password), bcrypt.DefaultCost)
	if err != nil {
		return uuid.UUID{}, fmt.Errorf("error generating password hash: %w", err)
	}

	user := entities.User{
		ID:             uuid.New(),
		Email:          string(request.Body.Email),
		Phone:          convert.ToNullString(request.Body.Phone),
		Name:           request.Body.Name,
		HashedPassword: string(hashedPassword),
		CreatedAt:      time.Now(),
		UpdatedAt:      time.Now(),
	}

	err = s.usersRepository.CreateUser(ctx, user)
	if err != nil {
		return uuid.UUID{}, fmt.Errorf("error creating user: %w", err)
	}

	return user.ID, nil
}

func (s *Service) UpdateUser(ctx context.Context, request server.UpdateUserRequestObject) (server.User, error) {
	user, err := s.usersRepository.GetUser(ctx, request.Params.XUserId)
	if err != nil {
		return server.User{}, err
	}

	user.Name = convert.UnwrapOr(request.Body.Name, user.Name)
	if request.Body.Phone != nil {
		user.Phone = convert.ToNullString(request.Body.Phone)
	}

	err = s.usersRepository.UpdateUser(ctx, user)
	if err != nil {
		return server.User{}, fmt.Errorf("error updating user: %w", err)
	}

	return server.User{
		Id:        user.ID,
		Email:     user.Email,
		Phone:     convert.FromNullString(user.Phone),
		Name:      user.Name,
		CreatedAt: user.CreatedAt,
		UpdatedAt: user.UpdatedAt,
	}, nil
}

func (s *Service) GetUserInfo(ctx context.Context, userID uuid.UUID) (server.User, error) {
	user, err := s.usersRepository.GetUser(ctx, userID)
	if err != nil {
		if errors.Is(err, sql.ErrNoRows) {
			return server.User{}, ErrUserNotFound
		}
		return server.User{}, err
	}

	return server.User{
		Id:        user.ID,
		Email:     user.Email,
		Phone:     convert.FromNullString(user.Phone),
		Name:      user.Name,
		CreatedAt: user.CreatedAt,
		UpdatedAt: user.UpdatedAt,
	}, nil
}

func (s *Service) LoginUser(ctx context.Context, request server.LoginUserRequestObject) (server.UserLoginResponse, error) {
	// Получаем пользователя по email
	user, err := s.usersRepository.GetUserByEmail(ctx, string(request.Body.Email))
	if err != nil {
		if errors.Is(err, sql.ErrNoRows) {
			return server.UserLoginResponse{}, ErrInvalidCredentials
		}
		return server.UserLoginResponse{}, fmt.Errorf("error getting user: %w", err)
	}

	// Проверяем пароль
	err = bcrypt.CompareHashAndPassword([]byte(user.HashedPassword), []byte(request.Body.Password))
	if err != nil {
		return server.UserLoginResponse{}, ErrInvalidCredentials
	}

	// Генерируем JWT токен
	token, expiresAt, err := s.jwtManager.GenerateToken(user.ID, user.Email)
	if err != nil {
		return server.UserLoginResponse{}, fmt.Errorf("error generating token: %w", err)
	}

	// Возвращаем ответ
	return server.UserLoginResponse{
		User: server.User{
			Id:        user.ID,
			Email:     user.Email,
			Phone:     convert.FromNullString(user.Phone),
			Name:      user.Name,
			CreatedAt: user.CreatedAt,
			UpdatedAt: user.UpdatedAt,
		},
		Token:     token,
		ExpiresAt: expiresAt,
	}, nil
}
